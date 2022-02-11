package com.vitechteam.sdlc.env.model.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.vitechteam.sdlc.env.model.Environment;

@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnvironmentConfig(
        String key,
        String owner,
        String repository,
        String gitServer,
        String gitKind,
        String gitUrl,
        boolean remoteCluster,
        PromotionStrategy promotionStrategy,
        String namespace
) {

    public EnvironmentConfig(String key, PromotionStrategy promotionStrategy) {
        this(key, null, null, null, null, null, false, promotionStrategy, null);
    }

    @JsonIgnore
    public boolean isDev() {
        return Environment.DEV_ENV_KEY.equals(this.key);
    }
}
