package com.vitechteam.sdlc.api;

import com.vitechteam.sdlc.scm.User;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Value
@RestController
@RequestMapping("user")
public class UserRest {

    ScmResolver scmResolver;

    @GetMapping
    User user(Principal principal) {
        return this.scmResolver.resolve(principal).currentUser();
    }

}
