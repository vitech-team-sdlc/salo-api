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
import java.util.Optional;

@AllArgsConstructor
@Log4j2
public class DefaultSaloService implements SaloService {

    private final Scm scm;

    @Override
    public Salo save(Salo newSalo) {

        final List<Environment> environments = newSalo
                .getEnvironments()
                .stream()
                .filter(Environment::needsEnvironmentRepoCreation)
                .map(envToCreate -> {
                    log.info("Processing next environment {} for {}", envToCreate.getConfig().getKey(), newSalo.getName());
                    Environment env = createEnvironmentRepo(newSalo, envToCreate);
                    env = createInfraRepo(newSalo, env, newSalo.getIngressConfig());
                    createSecrets(env, env.getCluster().getRepository());
                    return env;
                })
                .toList();

        final Salo salo = newSalo.toBuilder()
                .environments(environments)
                .build();

        return configureDevEnvironment(salo);
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
                .getEnvironments()
                .stream()
                .filter(Environment::needsEnvironmentRepoCreation)
                .map(Environment::getCluster)
                .forEach(cluster -> this.scm.triggerInfrastructureUpdate(
                        cluster.getRepository(),
                        new UpdateInfrastructureParams(cluster.getRegion(), doApply, doDestroy)
                ));
    }

    @Nonnull
    private Salo configureDevEnvironment(@Nonnull Salo salo) {
        final Environment devEnvironment = salo.findDevEnvironment();
        final JxRequirements jxRequirements = this.scm.getJxRequirements(devEnvironment.envRepository());

        jxRequirements.spec().getIngress().setDomain(
                String.format("%s.%s", salo.getName(), salo.getIngressConfig().getDomain())
        );
        jxRequirements.spec().setEnvironments(salo.getEnvironments().stream().map(Environment::getConfig).toList());

        this.scm.updateJxRequirements(jxRequirements, devEnvironment.envRepository());
        return salo;
    }

    private void createSecrets(Environment environment, Repository repository) {
        this.scm.createSecret("AWS_ACCESS_KEY_ID", environment.getCluster().getCloudProviderClientId(), repository);
        this.scm.createSecret("AWS_SECRET_ACCESS_KEY", environment.getCluster().getCloudProviderSecret(), repository);
        this.scm.createSecret("ACCESS_TOKEN", scm.generateAccessToken(), repository);
    }

    private Environment createEnvironmentRepo(Salo salo, Environment environment) {
        final EnvironmentConfig config = environment.getConfig();
        final Repository template = config.isRemoteCluster() ? Scm.REMOTE_ENV_REPO_TEMPLATE : Scm.ENV_REPO_TEMPLATE;
        final Repository repository = this.scm.create(new Repository(
                        String.format("env-%s-%s", salo.getName(), config.getKey()),
                        salo.getOrganization()
                ),
                template
        );

        log.info("new environment repo created {} using template {}", repository.fullName(), template);
        return environment.withConfig(
                config.toBuilder()
                        .owner(salo.getOrganization())
                        .repository(repository.name())
                        .gitUrl(repository.url())
                        .build()
        );
    }

    private Environment createInfraRepo(Salo salo, Environment environment, IngressConfig ingress) {
        final Repository repository = this.scm.create(new Repository(
                        String.format("infra-%s-%s", salo.getName(), environment.getConfig().getKey()),
                        environment.getConfig().getOwner()
                ),
                Scm.INFRA_TEMPLATE
        );
        log.info("new infra repo created {} using template {}", repository.fullName(), Scm.INFRA_TEMPLATE);

        final Cluster newCluster = environment
                .getCluster()
                .withName(salo.getName())
                .withJxBotUsername(scm.currentUser().id())
                .withRepository(repository);
        final Environment newEnvironment = environment.withCluster(newCluster);

        final TfVars tfVars = this.scm.getTfVars(repository);
        tfVars.mergeWith(newEnvironment, ingress);
        this.scm.updateTfVars(tfVars, repository);

        log.info("new tf vars generated for {} salo: {}", repository.fullName(), salo.getName());

        return newEnvironment;
    }

    @Override
    public Collection<Salo> findByOrganization(String organization) {
        final Collection<Repository> repositories = this.scm.findRepositoriesByOrg(organization);
        return repositories.stream()
                .filter(this::isDev)
                .map(envRepo -> {
                    final JxRequirements jxRequirements = this.scm.getJxRequirements(envRepo);
                    final String saloName = envRepo.saloName();
                    final List<Environment> environments = jxRequirements
                            .spec()
                            .getEnvironments()
                            .stream()
                            .filter(ec -> ec.isRemoteCluster() || ec.isDev())
                            .map(jxEnv -> {
                                final Cluster cluster = findCluster(saloName, jxEnv, repositories);
                                return Environment.builder()
                                        .cluster(cluster)
                                        .config(jxEnv)
                                        .build();
                            })
                            .toList();
                    if (environments.isEmpty()) {
                        throw new IllegalStateException("incorrect env configuration, envs can't be empty");
                    }
                    return Salo.builder()
                            .name(saloName)
                            .organization(organization)
                            .cloudProvider(CloudProvider.AWS)
                            .ingressConfig(jxRequirements.spec().getIngress())
                            .environments(environments)
                            .build();
                })
                .toList();
    }

    @Override
    public Optional<Salo> findByNameAndOrg(String saloName, String organization) {
        return this.findByOrganization(organization)
                .stream()
                .filter(s -> s.getName().equals(saloName))
                .findFirst();
    }

    @Override
    public Optional<Salo> findStatusByNameAndOrg(String name, String organization) {
        return this.findByNameAndOrg(name, organization)
                .map(salo -> {
                    final List<Environment> environments = salo.getEnvironments().stream()
                            .filter(Environment::needsEnvironmentRepoCreation)
                            .map(env -> this.scm.findLatestInfraPipelineStatus(env.getCluster().getRepository())
                                    .map(Environment.Status::of)
                                    .map(env::withStatus)
                                    .orElse(env))
                            .toList();
                    return salo.toBuilder()
                            .environments(environments)
                            .build();
                });
    }

    @Nonnull
    private Cluster findCluster(String saloName, EnvironmentConfig environmentConfig, Collection<Repository> repositories) {
        final String lookingForName = String.format("infra-%s-%s", saloName, environmentConfig.getKey());
        final Repository infraRepo = repositories
                .stream()
                .filter(r -> r.name().equals(lookingForName))
                .findFirst()
                .orElseThrow(() -> new InfraRepositoryNotFound(String.format(
                        "can't find infra repository for installation: %s by name %s", saloName, lookingForName)
                ));

        final TfVars tfVars = this.scm.getTfVars(infraRepo);
        final List<NodeGroup> nodeGroups = new ArrayList<>();
        tfVars.getWorkers().forEach((name, worker) -> nodeGroups.add(
                NodeGroup.builder()
                        .name(name)
                        .maxSize(worker.getAsgMaxSize())
                        .minSize(worker.getAsgMinSize())
                        .spotSize(worker.getAsgMaxSize() - worker.getOnDemandBaseCapacity())
                        .labels(worker.getK8SLabels())
                        .taints(worker.getK8STaints())
                        .tags(worker.getTags())
                        .volumeSize(worker.getRootVolumeSize())
                        .vmTypes(worker.getOverrideInstanceTypes())
                        .build()
        ));

        return Cluster.builder()
                .name(tfVars.getClusterName())
                .jxBotUsername(tfVars.getJxBotUsername())
                .region(tfVars.getRegion())
                .nodeGroups(nodeGroups)
                .repository(infraRepo)
                .build();
    }

    private boolean isDev(Repository repo) {
        return repo.name().startsWith("env-") && repo.name().endsWith(Environment.DEV_ENV_KEY);
    }

    private static class InfraRepositoryNotFound extends RuntimeException {
        public InfraRepositoryNotFound(String message) {
            super(message);
        }
    }
}
