package com.vitechteam.sdlc.env.model;

import com.vitechteam.sdlc.env.model.config.IngressConfig;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class Salo {
    String name;
    CloudProvider cloudProvider;
    String organization;
    IngressConfig ingressConfig;
    @Builder.Default
    Collection<Environment> environments = List.of();

    public Environment findDevEnvironment() {
        return environments.stream().filter(Environment::isDev).findFirst().orElseThrow();
    }
}
