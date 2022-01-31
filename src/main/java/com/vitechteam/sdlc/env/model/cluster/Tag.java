package com.vitechteam.sdlc.env.model.cluster;

import java.util.Collection;
import java.util.List;

public record Tag(String key, String value, boolean propagateAtLaunch) {

  public static Collection<Tag> of(String k, String v, boolean propagateAtLaunch) {
    return List.of(new Tag(k, v, propagateAtLaunch));
  }
}
