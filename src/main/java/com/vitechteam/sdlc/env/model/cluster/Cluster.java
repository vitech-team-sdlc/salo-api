package com.vitechteam.sdlc.env.model.cluster;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
  @JsonIgnore
  String cloudProviderClientId;
  @JsonIgnore
  String cloudProviderSecret;
  String cloudProfile;
  String region;
  Collection<NodeGroup> nodeGroups;
  @With
  Repository repository;

  public static Cluster of(NodeGroup... nodeGroups){
    return Cluster.builder().nodeGroups(List.of(nodeGroups)).build();
  }
}
