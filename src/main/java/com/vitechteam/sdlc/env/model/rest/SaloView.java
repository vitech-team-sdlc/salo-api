package com.vitechteam.sdlc.env.model.rest;


import com.vitechteam.sdlc.env.model.CloudProvider;
import com.vitechteam.sdlc.env.model.PromotionStrategy;
import com.vitechteam.sdlc.env.model.cluster.Label;
import com.vitechteam.sdlc.env.model.cluster.Tag;
import com.vitechteam.sdlc.env.model.cluster.Taint;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.List;

@Value
@Builder
public class SaloView {
    String name;
    String organization;
    CloudProvider cloudProvider;
    DomainView domain;
    @Builder.Default
    Collection<EnvironmentView> environments = List.of();

    @Value
    @Builder
    public static class DomainView {
        String name;
        TlsView tls;

        @Value
        @Builder
        public static class TlsView {
            boolean enabled;
            String email;
            boolean production;
        }
    }

    @Value
    @Builder
    public static class EnvironmentView {
        ClusterView cluster;
        ConfigView config;

        @Value
        @Builder
        public static class ClusterView {
            String name;
            String region;
            boolean domainOwner;
            @Builder.Default
            Collection<NodeGroupView> nodeGroups = List.of();

            @Value
            @Builder
            public static class NodeGroupView {
                String name;
                int maxSize;
                int minSize;
                int spotSize;
                int volumeSize;
                @Builder.Default
                Collection<String> vmTypes = List.of();
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
        public static class ConfigView {
            String key;
            boolean remoteCluster;
            PromotionStrategy promotionStrategy;
        }
    }
}
