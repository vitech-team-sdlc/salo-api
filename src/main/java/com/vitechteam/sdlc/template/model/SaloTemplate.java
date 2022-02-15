package com.vitechteam.sdlc.template.model;

import com.vitechteam.sdlc.env.model.config.PromotionStrategy;

import java.util.List;

public record SaloTemplate(
        String name,
        List<EnvironmentTemplate> environments
) {
    public record EnvironmentTemplate(
            String name,
            ClusterTemplate cluster,
            ConfigTemplate config
    ) {
        public record ClusterTemplate(
                List<NodeGroupTemplate> nodeGroups
        ) {
            public record NodeGroupTemplate(
                    String name,
                    int maxSize,
                    int minSize,
                    int spotSize,
                    int volumeSize,
                    List<String> vmTypes
            ) {
            }
        }

        public record ConfigTemplate(
                String key,
                boolean remoteCluster,
                PromotionStrategy promotionStrategy
        ) {
        }
    }
}
