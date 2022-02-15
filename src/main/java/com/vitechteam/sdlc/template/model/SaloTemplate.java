package com.vitechteam.sdlc.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vitechteam.sdlc.env.model.cluster.Label;
import com.vitechteam.sdlc.env.model.cluster.Tag;
import com.vitechteam.sdlc.env.model.cluster.Taint;
import com.vitechteam.sdlc.env.model.config.PromotionStrategy;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SaloTemplate(
        String name,
        Collection<EnvironmentTemplate> environments
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EnvironmentTemplate(
            String name,
            ClusterTemplate cluster,
            ConfigTemplate config
    ) {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record ClusterTemplate(
                Collection<NodeGroupTemplate> nodeGroups
        ) {
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record NodeGroupTemplate(
                    String name,
                    int maxSize,
                    int minSize,
                    int spotSize,
                    int volumeSize,
                    Collection<String> vmTypes,
                    Collection<Label> labels,
                    Collection<Taint> taints,
                    Collection<Tag> tags
            ) {
            }
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record ConfigTemplate(
                String key,
                boolean remoteCluster,
                PromotionStrategy promotionStrategy
        ) {
        }
    }
}
