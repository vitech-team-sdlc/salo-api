package com.vitechteam.sdlc.env.model.config;

public record EnvironmentConfig(
        String key,
        String owner,
        String repository,
        String gitServer,
        String gitKind,
        String gitURL,
        boolean remoteCluster,
        PromotionStrategy promotionStrategy,
        String namespace
) {

    public EnvironmentConfig(String key, PromotionStrategy promotionStrategy) {
        this(key, null, null, null, null, null, false, promotionStrategy, null);
    }
}
