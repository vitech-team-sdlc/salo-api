package com.vitechteam.sdlc.env.model.cluster;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vitechteam.sdlc.scm.Repository;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;
import java.util.List;

@Value
@Builder
@Jacksonized
public class Cluster {
    @With
    String name;
    @With
    String jxBotUsername;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String cloudProviderClientId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String cloudProviderSecret;
    boolean domainOwner;
    String cloudProfile;
    String region;
    @Builder.Default
    Collection<NodeGroup> nodeGroups = List.of();
    @With
    Repository repository;

    public static Cluster of(NodeGroup... nodeGroups) {
        return Cluster.builder().nodeGroups(List.of(nodeGroups)).build();
    }
}
