package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.env.model.Environment;
import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.config.EnvironmentConfig;
import com.vitechteam.sdlc.env.model.config.IngressConfig;
import com.vitechteam.sdlc.env.model.config.JxRequirements;
import com.vitechteam.sdlc.env.model.tf.TfVars;
import com.vitechteam.sdlc.scm.Repository;
import com.vitechteam.sdlc.scm.Scm;
import com.vitechteam.sdlc.scm.UpdateInfrastructureParams;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;
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
    public Collection<Salo> findAll() {
        throw new NotImplementedException();
    }
}
