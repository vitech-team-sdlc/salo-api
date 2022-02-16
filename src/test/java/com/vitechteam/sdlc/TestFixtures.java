package com.vitechteam.sdlc;

import com.vitechteam.sdlc.env.model.CloudProvider;
import com.vitechteam.sdlc.env.model.Environment;
import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.cluster.NodeGroup;
import com.vitechteam.sdlc.env.model.config.EnvironmentConfig;
import com.vitechteam.sdlc.env.model.config.IngressConfig;
import com.vitechteam.sdlc.env.model.PromotionStrategy;

import java.util.List;
import java.util.Map;

public interface TestFixtures {

    default Salo newSalo(
            final String name,
            final String organization,
            final String cloudClientId,
            final String cloudSecret
    ) {
        final var ingressConfig = IngressConfig.builder()
                .externalDNS(true)
                .cloudDNSSecretName("")
                .domain("sdlcvitech.com")
                .ignoreLoadBalancer(false)
                .namespaceSubDomain("")
                .tls(IngressConfig.TLSConfig.builder()
                        .enabled(true)
                        .email("volodymyr.kvych@vitechteam.com")
                        .production(true)
                        .build())
                .annotations(Map.of())
                .build();

        final var devCluster = Cluster.builder()
                .name(name + "-cluster")
                .jxBotUsername("vkvych")
                .cloudProviderClientId(cloudClientId)
                .cloudProviderSecret(cloudSecret)
                .domainOwner(true)
                .region("us-east-2")
                .nodeGroups(List.of(
                        newNodeGroup("pipelines"),
                        newNodeGroup("main")
                ))
                .build();

        final var devEnvironment = Environment.builder()
                .cluster(devCluster)
                .config(EnvironmentConfig.builder()
                        .key(Environment.DEV_ENV_KEY)
                        .promotionStrategy(PromotionStrategy.Auto)
                        .build())
                .build();
//        final var stagingEnvironment = new Environment(new EnvironmentConfig("STG", PromotionStrategy.Auto));
//        final var prdEnvironment = new Environment(new EnvironmentConfig("PRD", PromotionStrategy.Auto));

        return Salo.builder()
                .name(name)
                .organization(organization)
                .cloudProvider(CloudProvider.AWS)
                .ingressConfig(ingressConfig)
                .environments(List.of(devEnvironment))
                .build();
    }

    default NodeGroup newNodeGroup(final String name) {
        return NodeGroup.builder()
                .name(name)
                .maxSize(2)
                .minSize(1)
                .spotSize(1)
                .volumeSize(60)
                .vmTypes(List.of(
                        "m5.xlarge"
                ))
                .build();
    }
}
