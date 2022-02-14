package com.vitechteam.sdlc.env.model.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JxRequirements(String apiVersion, String kind, RequirementsConfig spec) {
}
