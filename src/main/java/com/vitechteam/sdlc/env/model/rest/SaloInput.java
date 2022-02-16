package com.vitechteam.sdlc.env.model.rest;

import com.vitechteam.sdlc.env.model.CloudProvider;
import com.vitechteam.sdlc.env.model.PromotionStrategy;
import com.vitechteam.sdlc.env.model.cluster.Label;
import com.vitechteam.sdlc.env.model.cluster.Tag;
import com.vitechteam.sdlc.env.model.cluster.Taint;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;

@Value
@Builder
@Jacksonized
public class SaloInput {
    @NotBlank String name;
    @NotBlank String organization;
    @NotNull CloudProvider cloudProvider;
    @Valid @NotNull DomainInput domain;
    @Builder.Default
    @Size(min = 1) Collection<@Valid EnvironmentInput> environments = List.of();

    @Value
    @Builder
    @Jacksonized
    public static class DomainInput {
        @NotBlank String name;
        @NotNull TlsInput tls;

        @Value
        @Builder
        @Jacksonized
        public static class TlsInput {
            boolean enabled;
            @NotBlank String email;
            boolean production;
        }
    }

    @Value
    @Builder
    @Jacksonized
    public static class EnvironmentInput {
        @Valid @NotNull ClusterInput cluster;
        @Valid @NotNull ConfigInput config;

        @Value
        @Builder
        @Jacksonized
        public static class ClusterInput {
            @NotBlank String name;
            @NotBlank String cloudProviderClientId;
            @NotBlank String cloudProviderSecret;
            @NotBlank String region;
            boolean domainOwner;
            @Builder.Default
            @Size(min = 2) Collection<@Valid NodeGroupInput> nodeGroups = List.of();

            @Value
            @Builder
            @Jacksonized
            public static class NodeGroupInput {
                @NotBlank String name;
                @Positive int maxSize;
                @Positive int minSize;
                @PositiveOrZero int spotSize;
                @Positive int volumeSize;
                @Builder.Default
                @NotEmpty Collection<String> vmTypes = List.of();
                @Builder.Default
                Collection<Label> labels = List.of();
                @Builder.Default
                Collection<Taint> taints = List.of();
                @Builder.Default
                Collection<Tag> tags = List.of();
            }
        }

        @Value
        @Builder
        @Jacksonized
        public static class ConfigInput {
            @NotBlank String key;
            boolean remoteCluster;
            @NotNull PromotionStrategy promotionStrategy;
        }
    }
}
