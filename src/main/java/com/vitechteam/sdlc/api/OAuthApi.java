package com.vitechteam.sdlc.api;

import com.vitechteam.sdlc.api.config.GitHubAccessToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("oauth")
public class OAuthApi {

    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;
    private final RestTemplate restTemplate;

    @Autowired
    public OAuthApi(OAuth2ResourceServerProperties oAuth2ResourceServerProperties) {
        this.oAuth2ResourceServerProperties = oAuth2ResourceServerProperties;
        this.restTemplate = new RestTemplateBuilder().rootUri("https://github.com/").build();
    }

    public OAuthApi(OAuth2ResourceServerProperties oAuth2ResourceServerProperties, RestTemplate restTemplate) {
        this.oAuth2ResourceServerProperties = oAuth2ResourceServerProperties;
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/access_token", method = {
            RequestMethod.POST,
            RequestMethod.GET,
            RequestMethod.PATCH,
            RequestMethod.PUT
    })
    public GitHubAccessToken accessToken(@RequestParam String code) {
        return this.restTemplate
                .postForObject(
                        "/login/oauth/access_token?client_id={client_id}&client_secret={client_secret}&code={code}",
                        null,
                        GitHubAccessToken.class,
                        Map.of(
                                "client_id", this.oAuth2ResourceServerProperties.getOpaquetoken().getClientId(),
                                "client_secret", this.oAuth2ResourceServerProperties.getOpaquetoken().getClientSecret(),
                                "code", code
                        )
                );
    }

}
