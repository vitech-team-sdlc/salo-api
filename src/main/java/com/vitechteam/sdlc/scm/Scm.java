package com.vitechteam.sdlc.scm;

import com.vitechteam.sdlc.env.model.config.JxRequirements;
import com.vitechteam.sdlc.env.model.tf.TfVars;

import java.util.Collection;

public interface Scm {

    String ORG_TEMPLATE = "vitech-team-sdlc";

    Repository INFRA_TEMPLATE = new Repository(
            "jx3-terraform-eks",
            ORG_TEMPLATE,
            "main",
            ""
    );

    Repository ENV_REPO_TEMPLATE = new Repository(
            "jx3-eks-vault",
            ORG_TEMPLATE,
            "main",
            ""
    );

    Repository REMOTE_ENV_REPO_TEMPLATE = new Repository(
            "jx3-kubernetes-production",
            ORG_TEMPLATE,
            "main",
            ""
    );

    ScmProvider provider();

    User currentUser();

    User findUser(String id);

    Collection<Organization> findAllOrganizations();

    Collection<Repository> findAllRepositories();

    Repository create(Repository newRepo, Repository template);

    void createSecret(String key, String value, Repository repository);

    String generateAccessToken();

    JxRequirements getJxRequirements(Repository envRepository);

    JxRequirements updateJxRequirements(JxRequirements requirementsConfig, Repository envRepository);

    TfVars getTfVars(Repository repository);

    void updateTfVars(TfVars tfVars, Repository repository);

    File findFile(File file, Repository repository);

    File updateFile(File file, Repository repository, String commitMessage);

    void triggerInfrastructureUpdate(Repository repository, UpdateInfrastructureParams updateInfrastructureParams);
}
