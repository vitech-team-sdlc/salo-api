package com.vitechteam.sdlc.env.model;

import com.vitechteam.sdlc.scm.PipelineStatus;
import com.vitechteam.sdlc.scm.Repository;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

public record SaloStatus(
        String name,
        String organization,
        Status status,
        Collection<Environment> environments
) {
    public enum Status {
        Ok,
        Pending,
        InProgress,
        Error,
        Unknown
    }

    public record Environment(
            String name,
            ClusterStatus infrastructure,
            EnvironmentStatus environment
    ) {
        public static Environment of(
                com.vitechteam.sdlc.env.model.Environment environment,
                PipelineStatus infraPipelineStatus
        ) {
            return new Environment(
                    environment.cluster().getName(),
                    ClusterStatus.of(environment.envRepository(), infraPipelineStatus),
                    EnvironmentStatus.of(environment.cluster().getRepository())
            );
        }

        public record ClusterStatus(
                Status status,
                Repository repository,
                PipelineStatus pipeline
        ) {
            public static ClusterStatus of(
                    Repository repository,
                    @Nullable PipelineStatus pipelineStatus
            ) {
                return new ClusterStatus(
                        Optional.ofNullable(pipelineStatus)
                                .map(status -> switch (status.status()) {
                                    case "QUEUED" -> Status.Pending;
                                    case "IN_PROGRESS" -> Status.InProgress;
                                    case "COMPLETED" -> switch (status.conclusion()) {
                                        case "SUCCESS" -> Status.Ok;
                                        default -> Status.Error;
                                    };
                                    default -> Status.Error;
                                })
                                .orElse(Status.Pending),
                        repository,
                        pipelineStatus
                );
            }
        }

        public record EnvironmentStatus(
                Status status,
                Repository repository
        ) {
            public static EnvironmentStatus of(Repository repository) {
                return new EnvironmentStatus(
                        Status.Pending,
                        repository
                );
            }
        }
    }
}
