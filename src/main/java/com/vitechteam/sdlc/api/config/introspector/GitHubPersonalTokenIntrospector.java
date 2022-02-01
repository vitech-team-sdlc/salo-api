package com.vitechteam.sdlc.api.config.introspector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitechteam.sdlc.scm.ScmProvider;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

public class GitHubPersonalTokenIntrospector extends NimbusOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    public GitHubPersonalTokenIntrospector(OAuth2ResourceServerProperties properties) {
        super(
                properties.getOpaquetoken().getIntrospectionUri(),
                new RestTemplateBuilder()
                        .defaultHeader(HttpHeaders.AUTHORIZATION, properties.getOpaquetoken().getClientId() + " " + properties.getOpaquetoken().getClientSecret())
                        .additionalInterceptors(getClientHttpRequestInterceptor())
                        .build()
        );
        setRequestEntityConverter(githubIntrospectMsgConverter(
                URI.create(properties.getOpaquetoken().getIntrospectionUri())
        ));
    }

    private static ClientHttpRequestInterceptor getClientHttpRequestInterceptor() {
        return (request, body, execution) -> {
            final ClientHttpResponse response = execution.execute(request, body);
            final var objectMapper = new ObjectMapper();

            if (!response.getStatusCode().is2xxSuccessful()) {
                final var responseBody = IOUtils.toInputStream(
                        objectMapper.writeValueAsString(Map.of("active", false)), Charset.defaultCharset()
                );
                return new HackedHttpResponse(response, responseBody);
            }

            final var map = objectMapper.readValue(response.getBody(), Map.class);
            map.put("active", true);
            map.put("scm", ScmProvider.GITHUB);
            final InputStream responseBody = IOUtils.toInputStream(
                    objectMapper.writeValueAsString(map), Charset.defaultCharset()
            );
            return new HackedHttpResponse(response, responseBody);
        };
    }

    private Converter<String, RequestEntity<?>> githubIntrospectMsgConverter(URI introspectionUri) {
        return token -> {
            final var headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            return new RequestEntity<>("", headers, HttpMethod.GET, introspectionUri);
        };
    }

}
