package com.vitechteam.sdlc.template.model;

import java.util.List;

public record SaloTemplate(
        String name,
        List<EnvironmentTemplate> environments
) {
}
