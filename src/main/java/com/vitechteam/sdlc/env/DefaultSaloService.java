package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.env.model.CloudProvider;
import com.vitechteam.sdlc.env.model.Environment;
import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.cluster.NodeGroup;
import com.vitechteam.sdlc.env.model.config.EnvironmentConfig;
import com.vitechteam.sdlc.env.model.config.IngressConfig;
import com.vitechteam.sdlc.env.model.config.JxRequirements;
import com.vitechteam.sdlc.env.model.tf.TfVars;
import com.vitechteam.sdlc.scm.Repository;
import com.vitechteam.sdlc.scm.Scm;
import com.vitechteam.sdlc.scm.UpdateInfrastructureParams;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        triggerInfraCreation(salo);

        return salo;
    }

    private void triggerInfraCreation(Salo salo) {
        salo
                .environments()
                .stream()
                .filter(Environment::needsEnvironmentRepoCreation)
                .forEach(env -> this.scm.triggerInfrastructureUpdate(
                        env.cluster().getRepository(),
                        new UpdateInfrastructureParams(
                                env.cluster().getRegion(),
                                false,
                                false
                        )
                ));
    }

    @Nonnull
    private Salo configureDevEnvironment(@Nonnull Salo salo) {
        final Environment devEnvironment = salo.findDevEnvironment();
        final JxRequirements jxRequirements = this.scm.getJxRequirements(devEnvironment.envRepository());

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
                .filter(this::isDev)
                .map(envRepo -> {
                    final JxRequirements jxRequirements = this.scm.getJxRequirements(envRepo);
                    final String saloName = parseSaloName(envRepo);
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
                .toList();
    }

    @Override
    public Salo findByNameAndOrg(String saloName, String organization) {
        return this.findByOrganization(organization)
                .stream()
                .filter(s -> s.name().equals(saloName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(organization + "/" + saloName + " not found"));
    }

    private static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    @Override
    public Salo findStatusByNameAndOrg(String name, String organization) {
        final Salo salo = this.findByNameAndOrg(name, organization);
        final List<Environment> environments = salo.environments().stream()
                .filter(Environment::needsEnvironmentRepoCreation)
                .map(env -> this.scm.getLatestInfraPipelineStatus(env.cluster().getRepository())
                        .map(st -> new Environment(env.cluster(), env.config(), new Environment.Status(st)))
                        .orElse(env)
                )
                .toList();
        return new Salo(
                salo.name(),
                salo.cloudProvider(),
                salo.organization(),
                salo.ingressConfig(),
                environments
        );
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

    private boolean isDev(Repository repo) {
        return repo.name().startsWith("env-") && repo.name().endsWith("DEV");
    }

    private String parseSaloName(Repository repo) {
        return repo.name().split("-")[1];
    }

    private static class InfraRepositoryNotFound extends RuntimeException {
        public InfraRepositoryNotFound(String message) {
            super(message);
        }
    }
}
