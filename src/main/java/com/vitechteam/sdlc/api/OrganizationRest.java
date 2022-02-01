package com.vitechteam.sdlc.api;

import com.vitechteam.sdlc.scm.Organization;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;

@Value
@RestController
@RequestMapping("/organization")
public class OrganizationRest {

    ScmResolver scmResolver;

    @GetMapping
    public Collection<Organization> findAll(Principal principal) {
        return this.scmResolver.resolve(principal).findAllOrganizations();
    }

}


