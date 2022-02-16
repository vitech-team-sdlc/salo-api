package com.vitechteam.sdlc.cost;

import com.vitechteam.sdlc.cost.model.CostView;
import com.vitechteam.sdlc.template.model.SaloTemplate;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@AllArgsConstructor
@RestController
@RequestMapping(value = "cost", produces = MediaType.APPLICATION_JSON_VALUE)
public class CostRest {

    private final Costs costs;

    @PostMapping("template")
    public CostView estimateForTemplate(
            @Valid @RequestBody SaloTemplate saloTemplate
    ) {
        return this.costs.estimateForTemplate(saloTemplate);
    }
}
