package com.vitechteam.sdlc.cost.model;

import java.math.BigDecimal;

public record ProductCost(
        long quantity,
        BigDecimal ratePerHour
) {
}
