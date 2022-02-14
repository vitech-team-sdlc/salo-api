package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.scm.ScmResolver;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;

@AllArgsConstructor
@Log4j2
@RestController
@RequestMapping("salo")
public class SaloRest {

    private final ScmResolver scmResolver;

    @GetMapping("{organization}")
    public Collection<Salo> findAll(
            Principal principal,
            @PathVariable String organization
    ) {
        return this.scmResolver.getSaloService(principal).findByOrganization(organization);
    }

    @PostMapping("{organization}")
    public Salo save(
            Principal principal,
            @RequestBody Salo salo,
            @PathVariable String organization
    ) {
        log.info("starting salo creation for org: {}", organization);
        return this.scmResolver.getSaloService(principal).save(salo);
    }

    @GetMapping("{organization}/{name}")
    public Salo findAll(
            Principal principal,
            @PathVariable String organization,
            @PathVariable String name
    ) {
        return this.scmResolver
                .getSaloService(principal)
                .findByNameAndOrg(name, organization)
                .orElseThrow(() -> new NotFoundException(organization + "/" + name + " not found"));
    }

    @GetMapping("{organization}/{name}/status")
    public Salo findStatus(
            Principal principal,
            @PathVariable String organization,
            @PathVariable String name
    ) {
        return this.scmResolver
                .getSaloService(principal)
                .findStatusByNameAndOrg(name, organization)
                .orElseThrow(() -> new NotFoundException(organization + "/" + name + " not found"));
    }

    @PostMapping("{organization}/{name}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public void apply(
            Principal principal,
            @PathVariable String organization,
            @PathVariable String name
    ) {
        log.info("deploying salo instance [{}] for org: {}", name, organization);
        final SaloService saloService = this.scmResolver.getSaloService(principal);
        saloService
                .findStatusByNameAndOrg(name, organization)
                .ifPresentOrElse(
                        saloService::applyInfrastructure,
                        () -> {
                            throw new NotFoundException(organization + "/" + name + " not found");
                        }
                );
    }

    @PostMapping("{organization}/{name}/destroy")
    @ResponseStatus(HttpStatus.CREATED)
    public void destroy(
            Principal principal,
            @PathVariable String organization,
            @PathVariable String name
    ) {
        log.info("destroying salo instance [{}] for org: {}", name, organization);
        final SaloService saloService = this.scmResolver.getSaloService(principal);
        saloService
                .findStatusByNameAndOrg(name, organization)
                .ifPresentOrElse(
                        saloService::destroyInfrastructure,
                        () -> {
                            throw new NotFoundException(organization + "/" + name + " not found");
                        }
                );
    }

    private static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
