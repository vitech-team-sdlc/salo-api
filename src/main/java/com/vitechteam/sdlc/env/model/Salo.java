package com.vitechteam.sdlc.env.model;

import com.vitechteam.sdlc.env.model.config.IngressConfig;

import java.util.Collection;

public record Salo(
  String name,
  CloudProvider cloudProvider,
  String organization,
  IngressConfig ingressConfig,
  Collection<Environment> environments
) {

  public Environment findDevEnvironment() {
    return environments.stream().filter(Environment::isDev).findFirst().orElseThrow();
  }
}
