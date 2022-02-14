package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.env.model.Salo;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@AllArgsConstructor
@Log4j2
@RestController
@RequestMapping("template")
public class TemplateRest {

    private final SaloTemplates saloTemplates;

    @GetMapping("{organization}")
    public Collection<Salo> save(@PathVariable String organization) {
        return this.saloTemplates.templates(organization);
    }
}
