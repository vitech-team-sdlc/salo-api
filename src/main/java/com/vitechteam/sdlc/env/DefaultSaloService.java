package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.env.model.CloudProvider;
import com.vitechteam.sdlc.env.model.Environment;
import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.env.model.SaloStatus;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.cluster.NodeGroup;
import com.vitechteam.sdlc.env.model.config.EnvironmentConfig;
import com.vitechteam.sdlc.env.model.config.IngressConfig;
import com.vitechteam.sdlc.env.model.config.JxRequirements;
import com.vitechteam.sdlc.env.model.tf.TfVars;
import com.vitechteam.sdlc.scm.PipelineStatus;
import com.vitechteam.sdlc.scm.Repository;
import com.vitechteam.sdlc.scm.Scm;
import com.vitechteam.sdlc.scm.UpdateInfrastructureParams;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@AllArgsConstructor
@Log4j2
public class DefaultSaloService implements SaloService {

    private final Scm scm;

    @Override
    public Salo save(Salo newSalo) {

        final List<Environment> environments = newSalo
                .environments()
                .stream()
                .filter(Environment::needsEnvironmentRepoCreation)
                .map(envToCreate -> {
                    log.info("Processing next environment {} for {}", envToCreate.config().key(), newSalo.name());
                    Environment env = createEnvironmentRepo(newSalo, envToCreate);
                    env = createInfraRepo(newSalo, env, newSalo.ingressConfig());
                    createSecrets(env, env.cluster().getRepository());
                    return env;
                })
                .toList();


        final Salo salo = configureDevEnvironment(new Salo(
                newSalo.name(),
                newSalo.cloudProvider(),
                newSalo.organization(),
                newSalo.ingressConfig(),
                environments
        ));

        return salo;
    }

    @Override
    public void applyInfrastructure(Salo salo) {
        triggerInfraPipeline(salo, true, false);
    }

    @Override
    public void destroyInfrastructure(Salo salo) {
        triggerInfraPipeline(salo, false, true);
    }

    private void triggerInfraPipeline(Salo salo, boolean doApply, boolean doDestroy) {
        salo
                .environments()
                .stream()
                .filter(Environment::needsEnvironmentRepoCreation)
                .map(Environment::cluster)
                .forEach(cluster -> this.scm.triggerInfrastructureUpdate(
                        cluster.getRepository(),
                        new UpdateInfrastructureParams(cluster.getRegion(), doApply, doDestroy)
                ));
    }

    @Nonnull
    private Salo configureDevEnvironment(@Nonnull Salo salo) {
        final Environment devEnvironment = salo.findDevEnvironment();
        final JxRequirements jxRequirements = this.scm.findJxRequirements(devEnvironment.envRepository()).orElseThrow();

        jxRequirements.spec().getIngress().setDomain(
                String.format("%s.%s", salo.name(), salo.ingressConfig().getDomain())
        );
        jxRequirements.spec().setEnvironments(salo.environments().stream().map(Environment::config).toList());

        this.scm.updateJxRequirements(jxRequirements, devEnvironment.envRepository());
        return salo;
    }

    private void createSecrets(Environment environment, Repository repository) {
        this.scm.createSecret("AWS_ACCESS_KEY_ID", environment.cluster().getCloudProviderClientId(), repository);
        this.scm.createSecret("AWS_SECRET_ACCESS_KEY", environment.cluster().getCloudProviderSecret(), repository);
        this.scm.createSecret("ACCESS_TOKEN", scm.generateAccessToken(), repository);
    }

    private Environment createEnvironmentRepo(Salo salo, Environment environment) {
        final Repository template = environment.config().remoteCluster() ? Scm.REMOTE_ENV_REPO_TEMPLATE : Scm.ENV_REPO_TEMPLATE;
        final Repository repository = this.scm.create(new Repository(
                        String.format("env-%s-%s", salo.name(), environment.config().key()),
                        salo.organization()
                ),
                template
        );
        final Environment environment1 = new Environment(
                environment.cluster(),
                new EnvironmentConfig(
                        environment.config().key(),
                        salo.organization(),
                        repository.name(),
                        environment.config().gitServer(),
                        environment.config().gitKind(),
                        repository.url(),
                        environment.config().remoteCluster(),
                        environment.config().promotionStrategy(),
                        environment.config().namespace()
                )
        );
        log.info("new environment repo created {} using template {}", repository.fullName(), template);
        return environment1;
    }

    private Environment createInfraRepo(Salo salo, Environment environment, IngressConfig ingress) {
        final Repository repository = this.scm.create(new Repository(
                        String.format("infra-%s-%s", salo.name(), environment.config().key()),
                        environment.config().owner()
                ),
                Scm.INFRA_TEMPLATE
        );
        log.info("new infra repo created {} using template {}", repository.fullName(), Scm.INFRA_TEMPLATE);

        final Cluster newCluster = environment
                .cluster()
                .withName(salo.name())
                .withJxBotUsername(scm.currentUser().id())
                .withRepository(repository);
        final Environment newEnvironment = new Environment(newCluster, environment.config());

        final TfVars tfVars = this.scm.getTfVars(repository);
        tfVars.mergeWith(newEnvironment, ingress);
        this.scm.updateTfVars(tfVars, repository);

        log.info("new tf vars generated for {} salo: {}", repository.fullName(), salo.name());

        return newEnvironment;
    }

    @Override
    public Collection<Salo> findByOrganization(String organization) {
        final Collection<Repository> repositories = this.scm.findRepositoriesByOrg(organization);
        return repositories.stream()
                // TODO: temporary - to be re-written with changes with installation look-up logic
                .filter(Repository::isSaloInstallation)
                .filter(Repository::isDev)
                .flatMap(envRepo -> this.scm.findJxRequirements(envRepo)
                        .map(jxRequirements -> {
                            final String saloName = envRepo.saloName();
                            final List<Environment> environments = jxRequirements
                                    .spec()
                                    .getEnvironments()
                                    .stream()
                                    .filter(ec -> ec.remoteCluster() || ec.isDev())
                                    .map(jxEnv -> {
                                        final Cluster cluster = findCluster(saloName, jxEnv, repositories);
                                        return new Environment(cluster, jxEnv);
                                    })
                                    .toList();
                            if (environments.isEmpty()) {
                                throw new IllegalStateException("incorrect env configuration, envs can't be empty");
                            }
                            return new Salo(
                                    saloName,
                                    CloudProvider.AWS,
                                    organization,
                                    jxRequirements.spec().getIngress(),
                                    environments
                            );
                        })
                        .stream())
                .toList();
    }

    @Override
    public Optional<Salo> findByNameAndOrg(String saloName, String organization) {
        return this.findByOrganization(organization)
                .stream()
                .filter(s -> s.name().equals(saloName))
                .findFirst();
    }

    @Override
    public Optional<SaloStatus> findStatusByNameAndOrg(String name, String organization) {
        return this.findByNameAndOrg(name, organization)
                .map(salo -> {
                    final List<SaloStatus.Environment> environments = salo.environments().stream()
                            .filter(Environment::needsEnvironmentRepoCreation)
                            .map(env -> {
                                final Optional<PipelineStatus> infraPipelineStatus = this.scm
                                        .findLatestInfraPipelineStatus(env.cluster().getRepository());

                                return SaloStatus.Environment.of(env, infraPipelineStatus.orElse(null));
                            })
                            .toList();
                    return new SaloStatus(
                            salo.name(),
                            salo.organization(),
                            environments.stream()
                                    .flatMap(env -> Stream.of(
                                            env.environment().status(),
                                            env.infrastructure().status()
                                    ))
                                    .max(SaloStatus.Status::compareTo)
                                    .orElse(SaloStatus.Status.Unknown),
                            environments
                    );
                });
    }

    @Nonnull
    private Cluster findCluster(String saloName, EnvironmentConfig environmentConfig, Collection<Repository> repositories) {
        final String lookingForName = String.format("infra-%s-%s", saloName, environmentConfig.key());
        final Repository infraRepo = repositories
                .stream()
                .filter(r -> r.name().equals(lookingForName))
                .findFirst()
                .orElseThrow(() -> new InfraRepositoryNotFound(String.format(
                        "can't find infra repository for installation: %s by name %s", saloName, lookingForName)
                ));

        final TfVars tfVars = this.scm.getTfVars(infraRepo);
        final List<NodeGroup> nodeGroups = new ArrayList<>();
        tfVars.getWorkers().forEach((name, worker) -> nodeGroups.add(new NodeGroup(
                name,
                worker.getAsgMaxSize(),
                worker.getAsgMinSize(),
                worker.getAsgMaxSize() - worker.getOnDemandBaseCapacity(),
                worker.getK8SLabels(),
                worker.getK8STaints(),
                worker.getTags(),
                worker.getRootVolumeSize(),
                worker.getOverrideInstanceTypes()
        )));

        return Cluster.builder()
                .name(tfVars.getClusterName())
                .jxBotUsername(tfVars.getJxBotUsername())
                .region(tfVars.getRegion())
                .nodeGroups(nodeGroups)
                .repository(infraRepo)
                .build();
    }

    private static class InfraRepositoryNotFound extends RuntimeException {
        public InfraRepositoryNotFound(String message) {
            super(message);
        }
    }
}
