package com.vitechteam.sdlc.env.model.cluster;

import java.util.Collection;
import java.util.List;

public record Taint(
  String key,
  String value,
  TaintEffect effect
) {

  public static Collection<Taint> of(String k, String v, TaintEffect effect) {
    return List.of(new Taint(k, v, effect));
  }
}
