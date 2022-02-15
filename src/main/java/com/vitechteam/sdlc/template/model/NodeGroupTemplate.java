package com.vitechteam.sdlc.template.model;

import java.util.List;

public record NodeGroupTemplate(
        String name,
        int maxSize,
        int minSize,
        int spotSize,
        int volumeSize,
        List<String> vmTypes
) {
}
