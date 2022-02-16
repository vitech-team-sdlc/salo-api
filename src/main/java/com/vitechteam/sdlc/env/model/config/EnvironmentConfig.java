package com.vitechteam.sdlc.env.model.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.vitechteam.sdlc.env.model.Environment;
import com.vitechteam.sdlc.env.model.PromotionStrategy;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnvironmentConfig {
    String key;
    String owner;
    String repository;
    String gitServer;
    String gitKind;
    String gitUrl;
    boolean remoteCluster;
    PromotionStrategy promotionStrategy;
    String namespace;

    @JsonIgnore
    public boolean isDev() {
        return Environment.DEV_ENV_KEY.equals(this.key);
    }
}
