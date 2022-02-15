package com.vitechteam.sdlc.template.model;

public record EnvironmentTemplate(
        String name,
        ClusterTemplate cluster,
        ConfigTemplate config
) {
}
