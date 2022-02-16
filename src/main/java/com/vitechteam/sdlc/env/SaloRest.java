package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.env.model.rest.SaloInput;
import com.vitechteam.sdlc.env.model.rest.SaloMapper;
import com.vitechteam.sdlc.env.model.rest.SaloStatusView;
import com.vitechteam.sdlc.env.model.rest.SaloView;
import com.vitechteam.sdlc.scm.ScmResolver;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Collection;

@AllArgsConstructor
@Log4j2
@RestController
@RequestMapping(value = "salo", produces = MediaType.APPLICATION_JSON_VALUE)
public class SaloRest {

    private final ScmResolver scmResolver;
    private final SaloMapper saloMapper;

    @GetMapping("{organization}")
    public Collection<SaloView> findAll(
            Principal principal,
            @PathVariable String organization
    ) {
        return this.scmResolver.getSaloService(principal)
                .findByOrganization(organization)
                .stream()
                .map(this.saloMapper::toView)
                .toList();
    }

    @PostMapping("{organization}")
    public SaloView save(
            Principal principal,
            @PathVariable String organization,
            @Valid @RequestBody SaloInput saloInput
    ) {
        log.info("starting salo creation for org: {}", organization);
        final Salo saloToCreate = this.saloMapper.toModel(saloInput);
        final Salo saloCreated = this.scmResolver.getSaloService(principal).save(saloToCreate);
        return this.saloMapper.toView(saloCreated);
    }

    @GetMapping("{organization}/{name}")
    public SaloView findAll(
            Principal principal,
            @PathVariable String organization,
            @PathVariable String name
    ) {
        return this.scmResolver
                .getSaloService(principal)
                .findByNameAndOrg(name, organization)
                .map(this.saloMapper::toView)
                .orElseThrow(() -> new NotFoundException(organization + "/" + name + " not found"));
    }

    @GetMapping("{organization}/{name}/status")
    public SaloStatusView findStatus(
            Principal principal,
            @PathVariable String organization,
            @PathVariable String name
    ) {
        return this.scmResolver
                .getSaloService(principal)
                .findStatusByNameAndOrg(name, organization)
                .map(this.saloMapper::toStatusView)
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
