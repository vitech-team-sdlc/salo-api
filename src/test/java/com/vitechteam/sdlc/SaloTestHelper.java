package com.vitechteam.sdlc;

import com.vitechteam.sdlc.env.model.CloudProvider;
import com.vitechteam.sdlc.env.model.Environment;
import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.cluster.Label;
import com.vitechteam.sdlc.env.model.cluster.NodeGroup;
import com.vitechteam.sdlc.env.model.cluster.Tag;
import com.vitechteam.sdlc.env.model.cluster.Taint;
import com.vitechteam.sdlc.env.model.cluster.TaintEffect;
import com.vitechteam.sdlc.env.model.config.EnvironmentConfig;
import com.vitechteam.sdlc.env.model.config.IngressConfig;
import com.vitechteam.sdlc.env.model.config.PromotionStrategy;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface SaloTestHelper {

    default Salo newDummySalo(String name) {
        final IngressConfig ingressConfig = new IngressConfig(
                true,
                "",
                "sdlcvitechteam.com",
                false,
                "",
                IngressConfig.TLSConfig.builder().enabled(true).email("serhiy.krupka@vitechteam.com").production(true).build(),
                Map.of()
        );
        final Cluster devCluster = Cluster
                .builder()
                .cloudProviderClientId("bbb")
                .cloudProviderSecret("bbb")
                .region("us-east-1")
                .nodeGroups(List.of(createNewNodeGroup("main"), createNewNodeGroup("pipelines")))
                .build();

        final Environment devEnvironment = new Environment(devCluster, new EnvironmentConfig("DEV", PromotionStrategy.Auto));
        final Environment stagingEnvironment = new Environment(new EnvironmentConfig("STG", PromotionStrategy.Auto));
        final Environment prdEnvironment = new Environment(new EnvironmentConfig("PRD", PromotionStrategy.Auto));

        return new Salo(
                name,
                CloudProvider.AWS,
                "vitech-team-sdlc",
                ingressConfig,
                List.of(devEnvironment, stagingEnvironment, prdEnvironment)
        );
    }

    default NodeGroup createNewNodeGroup(String name) {
        return new NodeGroup(
                name,
                3,
                1,
                1,
                List.of(new Label("k", "v")),
                List.of(new Taint("k", "v", TaintEffect.PreferNoSchedule)),
                List.of(new Tag("k", "v", true)),
                60,
                List.of(
                        "m5.xlarge",
                        "m5a.xlarge",
                        "m5d.xlarge",
                        "m5ad.xlarge",
                        "m5n.xlarge",
                        "m5.2xlarge",
                        "m5a.2xlarge",
                        "m5d.2xlarge",
                        "m5ad.2xlarge",
                        "m5n.2xlarge"
                )
        );
    }
}
