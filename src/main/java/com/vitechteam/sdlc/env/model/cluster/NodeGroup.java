package com.vitechteam.sdlc.env.model.cluster;

import javax.annotation.Nonnegative;
import java.util.Collection;
import java.util.List;

public record NodeGroup(
  String name,
  @Nonnegative
  int maxSize,
  @Nonnegative
  int minSize,
  @Nonnegative
  int spotSize,
  Collection<Label> labels,
  Collection<Taint> taints,
  Collection<Tag> tags,
  @Nonnegative
  int volumeSize,
  Collection<String> vmTypes
) {
  public NodeGroup(String name, int maxSize, int minSize, int spotSize, int volumeSize, List<String> vmTypes) {
    this(
      name,
      maxSize,
      minSize,
      spotSize,
      null,
      null,
      null,
      volumeSize,
      vmTypes
    );
  }
}
