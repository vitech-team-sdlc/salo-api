package com.vitechteam.sdlc.cost.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Month;

@Value
@Builder
public class CostView {
    BigDecimal maxPerDay;
    BigDecimal maxPerMonth;
}
