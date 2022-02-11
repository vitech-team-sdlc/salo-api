package com.vitechteam.sdlc.env;

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
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class SaloTemplates {

    public Collection<Salo> templates(String organization) {
        final IngressConfig ingressConfig = new IngressConfig(
                true,
                "sdlcvitechteam.com",
                IngressConfig.TLSConfig.builder().email("serhiy.krupka@vitechteam.com").build()
        );
        return List.of(
                new Salo(
                        "test",
                        CloudProvider.AWS,
                        organization,
                        ingressConfig,
                        Environment.list(
                                new Environment(Cluster.of(nodeMain(), nodePipeline()), createEnvironmentConfig(Environment.DEV_ENV_KEY, PromotionStrategy.Auto)),
                                new Environment(createEnvironmentConfig("STG", PromotionStrategy.Manual)),
                                new Environment(createEnvironmentConfig("PRD", PromotionStrategy.Manual))
                        )
                ),
                new Salo(
                        "test-2",
                        CloudProvider.AWS,
                        organization,
                        ingressConfig,
                        Environment.list(
                                new Environment(Cluster.of(nodeMain(), nodePipeline()), createEnvironmentConfig(Environment.DEV_ENV_KEY, PromotionStrategy.Auto)),
                                new Environment(createEnvironmentConfig("STG", PromotionStrategy.Auto)),
                                new Environment(createEnvironmentConfig("PRE-PRD", PromotionStrategy.Manual)),
                                new Environment(createEnvironmentConfig("PRD", PromotionStrategy.Manual))
                        )
                )
        );
    }

    public EnvironmentConfig createEnvironmentConfig(String key, PromotionStrategy promotionStrategy) {
        return new EnvironmentConfig(key, promotionStrategy);
    }

    private NodeGroup nodeMain() {
        return new NodeGroup(
                "pipelines",
                3,
                1,
                0,
                60,
                List.of("m5.large")
        );
    }


    private NodeGroup nodePipeline() {
        final String label = "pipeline";
        return new NodeGroup(
                "pipelines",
                1,
                1,
                1,
                Label.of(label, "true"),
                Taint.of(label, "true", TaintEffect.PreferNoSchedule),
                Tag.of(label, "true", true),
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
