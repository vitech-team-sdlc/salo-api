package com.vitechteam.sdlc.env.model.config;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

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

    @JsonIgnore
    public boolean isDev(){
        return Objects.equals(this.key.toLowerCase(), "dev");
    }
}
