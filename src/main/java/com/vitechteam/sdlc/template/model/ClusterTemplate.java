package com.vitechteam.sdlc.template.model;

import java.util.List;

public record ClusterTemplate(
        List<NodeGroupTemplate> nodeGroups
) {
}
