package com.vitechteam.sdlc.env.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.config.EnvironmentConfig;
import com.vitechteam.sdlc.scm.Repository;

import java.util.List;

public record Environment(
        Cluster cluster,
        EnvironmentConfig config
) {

    public Environment(EnvironmentConfig config) {
        this(null, config);
    }

    public boolean needsEnvironmentRepoCreation() {
        return isDev() || config.remoteCluster();
    }

    @JsonIgnore
    public boolean isDev() {
        return "DEV".equals(config.key());
    }

    public Repository envRepository() {
        return new Repository(
                config.repository(),
                config.owner(),
                Repository.DEFAULT_BRANCH,
                config.gitURL()
        );
    }

    public static List<Environment> list(Environment... environments) {
        return List.of(environments);
    }
}
