package com.vitechteam.sdlc;

import com.vitechteam.sdlc.env.model.CloudProvider;
import com.vitechteam.sdlc.env.model.Environment;
import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.cluster.NodeGroup;
import com.vitechteam.sdlc.env.model.config.EnvironmentConfig;
import com.vitechteam.sdlc.env.model.config.IngressConfig;
import com.vitechteam.sdlc.env.model.config.PromotionStrategy;

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

        final var devEnvironment = new Environment(
                devCluster,
                new EnvironmentConfig(Environment.DEV_ENV_KEY, PromotionStrategy.Auto)
        );
//        final var stagingEnvironment = new Environment(new EnvironmentConfig("STG", PromotionStrategy.Auto));
//        final var prdEnvironment = new Environment(new EnvironmentConfig("PRD", PromotionStrategy.Auto));

        return new Salo(
                name,
                CloudProvider.AWS,
                organization,
                ingressConfig,
                List.of(
                        devEnvironment/*,
                        stagingEnvironment,
                        prdEnvironment*/
                )
        );
    }

    default NodeGroup newNodeGroup(final String name) {
        return new NodeGroup(
                name,
                2,
                2,
                2,
                List.of(), //List.of(new Label("label-key", "label-value")),
                List.of(), //List.of(new Taint("taint-key", "taint-value", TaintEffect.PreferNoSchedule)),
                List.of(), //List.of(new Tag("tag-key", "tag-value", true)),
                30,
                List.of("m5a.large")
        );
    }
}
