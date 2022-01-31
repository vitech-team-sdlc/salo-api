package com.vitechteam.sdlc.env.model.config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonPropertyOrder(alphabetic = true)
public class RequirementsConfig {

  Map<String, Object> unknownFields =  new HashMap<>();

  List<EnvironmentConfig> environments;
  IngressConfig ingress;

  @JsonAnyGetter
  public Map<String, Object> getUnknownFields() {
    return unknownFields;
  }

  @JsonAnySetter
  public void setUnknownField(String name, Object value) {
    unknownFields.put(name, value);
  }

}
