package com.vitechteam.sdlc.cost.model;

import java.math.BigDecimal;

public record CostView(
        BigDecimal maxPerDay,
        BigDecimal maxPerMonth
) {
}
