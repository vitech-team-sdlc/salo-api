package com.vitechteam.sdlc.cost;

import com.vitechteam.sdlc.cost.model.CostView;
import com.vitechteam.sdlc.template.model.SaloTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class Costs {

    public CostView estimateForTemplate(SaloTemplate saloTemplate) {
        // TODO: un-mock:
        final int basePrise = 123 * saloTemplate.getEnvironments().size();
        return CostView.builder()
                .maxPerDay(BigDecimal
                        .valueOf(basePrise, 2)
                )
                .maxPerMonth(BigDecimal
                        .valueOf(basePrise, 2)
                        .multiply(BigDecimal.valueOf(31))
                )
                .build();
    }
}
