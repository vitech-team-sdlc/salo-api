package com.vitechteam.sdlc.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vitechteam.sdlc.env.model.PromotionStrategy;
import com.vitechteam.sdlc.env.model.cluster.Label;
import com.vitechteam.sdlc.env.model.cluster.Tag;
import com.vitechteam.sdlc.env.model.cluster.Taint;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaloTemplate {
    String name;
    Collection<EnvironmentTemplate> environments;

    @Value
    @Builder
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EnvironmentTemplate {
        ClusterTemplate cluster;
        ConfigTemplate config;

        @Value
        @Builder
        @Jacksonized
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class ClusterTemplate {
            String name;
            Collection<NodeGroupTemplate> nodeGroups;

            @Value
            @Builder
            @Jacksonized
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class NodeGroupTemplate {
                String name;
                int maxSize;
                int minSize;
                int spotSize;
                int volumeSize;
                Collection<String> vmTypes;
                Collection<Label> labels;
                Collection<Taint> taints;
                Collection<Tag> tags;
            }
        }

        @Value
        @Builder
        @Jacksonized
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class ConfigTemplate {
            String key;
            boolean remoteCluster;
            PromotionStrategy promotionStrategy;
        }
    }
}
