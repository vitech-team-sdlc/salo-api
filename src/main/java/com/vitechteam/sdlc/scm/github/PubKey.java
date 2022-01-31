package com.vitechteam.sdlc.scm.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PubKey(
  @JsonProperty("key_id")
  String keyId,
  String key
) {
}
