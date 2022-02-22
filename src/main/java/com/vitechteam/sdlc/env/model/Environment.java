package com.vitechteam.sdlc.env.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.config.EnvironmentConfig;
import com.vitechteam.sdlc.scm.Repository;
import lombok.Builder;

import java.util.List;

@Builder
public record Environment(
        Cluster cluster,
        EnvironmentConfig config
) {

    public static final String DEV_ENV_KEY = "dev";

    public Environment(EnvironmentConfig config) {
        this(null, config);
    }

    public boolean needsEnvironmentRepoCreation() {
        return isDev() || config.remoteCluster();
    }

    @JsonIgnore
    public boolean isDev() {
        return DEV_ENV_KEY.equals(config.key());
    }

    public Repository envRepository() {
        return new Repository(
                config.repository(),
                config.owner(),
                Repository.DEFAULT_BRANCH,
                config.gitUrl()
        );
    }

    public static List<Environment> list(Environment... environments) {
        return List.of(environments);
    }
}
