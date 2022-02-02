package com.vitechteam.sdlc.user;

import com.vitechteam.sdlc.scm.Organization;
import com.vitechteam.sdlc.scm.ScmResolver;
import com.vitechteam.sdlc.scm.User;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;

@Value
@RestController
@RequestMapping("user")
public class UserRest {

    ScmResolver scmResolver;

    @GetMapping
    User user(Principal principal) {
        return this.scmResolver.resolve(principal).currentUser();
    }

    @GetMapping("/organization")
    public Collection<Organization> userOrganizations(Principal principal) {
        return this.scmResolver.resolve(principal).findAllOrganizations();
    }
}
