package com.vitechteam.sdlc.template;

import com.vitechteam.sdlc.template.model.SaloTemplate;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@AllArgsConstructor
@Log4j2
@RestController
@RequestMapping(value = "templates", produces = MediaType.APPLICATION_JSON_VALUE)
class TemplateRest {

    private final Templates templates;

    @GetMapping
    Collection<SaloTemplate> defaults() {
        return this.templates.defaults();
    }
}
