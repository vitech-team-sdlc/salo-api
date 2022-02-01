package com.vitechteam.sdlc.api;

import com.vitechteam.sdlc.env.SaloTemplates;
import com.vitechteam.sdlc.env.model.Salo;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;

@Value
@Log4j2
@RestController
@RequestMapping("/salo")
public class SaloRest {

    ScmResolver scmResolver;
    SaloTemplates saloTemplates;

    @PostMapping("{organization}")
    public Salo save(
            @RequestBody Salo salo,
            @PathVariable String organization,
            Principal principal
    ) {
        log.info("starting salo creation for org: {}", organization);
        return this.scmResolver.getSaloService(principal).save(salo);
    }

    @GetMapping("/{organization}/templates")
    public Collection<Salo> save(@PathVariable String organization) {
        return this.saloTemplates.templates(organization);
    }

}
