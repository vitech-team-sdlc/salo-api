package com.vitechteam.sdlc.env.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IngressConfig {

    boolean externalDNS;
    @JsonProperty("cloud_dns_secret_name")
    String cloudDNSSecretName;
    String domain;
    boolean ignoreLoadBalancer;
    String namespaceSubDomain;
    TLSConfig tLS;
    Map<String, String> annotations;

    public IngressConfig(boolean externalDNS, String domain, TLSConfig tLS) {
        this.externalDNS = externalDNS;
        this.domain = domain;
        this.tLS = tLS;
    }

    @Value
    @Builder
    @Jacksonized
    public static class TLSConfig {
        boolean enabled;
        String email;
        // Production false uses self-signed certificates from the LetsEncrypt staging server, true enables the production
        // server which incurs higher rate limiting https://letsencrypt.org/docs/rate-limits/
        boolean production;
        // SecretName the name of the secret which contains the TLS certificate
        String secretName;
    }

}
