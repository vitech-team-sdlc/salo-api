package com.vitechteam.sdlc.env.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.config.EnvironmentConfig;
import com.vitechteam.sdlc.scm.PipelineStatus;
import com.vitechteam.sdlc.scm.Repository;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class Environment {
    public static final String DEV_ENV_KEY = "dev";

    @With Cluster cluster;
    @With EnvironmentConfig config;
    @With Status status;

    public boolean needsEnvironmentRepoCreation() {
        return isDev() || config.isRemoteCluster();
    }

    @JsonIgnore
    public boolean isDev() {
        return DEV_ENV_KEY.equals(config.getKey());
    }

    public Repository envRepository() {
        return new Repository(
                config.getRepository(),
                config.getOwner(),
                Repository.DEFAULT_BRANCH,
                config.getGitUrl()
        );
    }

    public static List<Environment> list(Environment... environments) {
        return List.of(environments);
    }

    @Value
    @Builder
    public static class Status {
        PipelineStatus infraPipelineStatus;

        public static Status of(PipelineStatus pipelineStatus) {
            return Environment.Status.builder()
                    .infraPipelineStatus(pipelineStatus)
                    .build();
        }
    }
}
