package com.vitechteam.sdlc.cost.model;

import java.math.BigDecimal;
import java.util.Map;

public record CostView(
        BigDecimal maxPerDay,
        BigDecimal maxPerMonth,
        Map<Product, ProductCost> products
) {
    private static final int HOURS_PER_DAY = 24;
    private static final int DAYS_PER_MONTH = 31;

    public static CostView of(Map<Product, ProductCost> products) {
        final BigDecimal maxPerDay = products.values().stream()
                .map(pc -> pc.ratePerHour()
                        .multiply(BigDecimal.valueOf(pc.quantity()))
                        .multiply(BigDecimal.valueOf(HOURS_PER_DAY)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal maxPerMonth = maxPerDay.multiply(BigDecimal.valueOf(DAYS_PER_MONTH));

        return new CostView(maxPerDay, maxPerMonth, Map.copyOf(products));
    }
}
