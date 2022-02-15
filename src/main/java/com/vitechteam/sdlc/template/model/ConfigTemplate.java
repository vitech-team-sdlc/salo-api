package com.vitechteam.sdlc.template.model;

import com.vitechteam.sdlc.env.model.config.PromotionStrategy;

public record ConfigTemplate(
        String key,
        boolean remoteCluster,
        PromotionStrategy promotionStrategy
) {
}
