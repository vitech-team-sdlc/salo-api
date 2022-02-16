package com.vitechteam.sdlc.env.model.rest;

import com.vitechteam.sdlc.env.model.Environment;
import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.config.EnvironmentConfig;
import com.vitechteam.sdlc.env.model.config.IngressConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface SaloMapper {

    @Mapping(target = "ingressConfig", source = "domain")
    @Mapping(target = "environments", source = "environments")
    Salo toModel(SaloInput input);

    @Mapping(target = "domain", source = "ingressConfig")
    SaloView toView(Salo input);

    SaloStatusView toStatusView(Salo input);

    @Mapping(target = "domain", source = "name")
    @Mapping(target = "externalDNS", ignore = true)
    @Mapping(target = "cloudDNSSecretName", ignore = true)
    @Mapping(target = "ignoreLoadBalancer", ignore = true)
    @Mapping(target = "namespaceSubDomain", ignore = true)
    @Mapping(target = "annotations", ignore = true)
    @Mapping(target = "tls.secretName", ignore = true)
    IngressConfig toModel(SaloInput.DomainInput input);

    @Mapping(target = "name", source = "domain")
    SaloView.DomainView toView(IngressConfig input);

    @Mapping(target = "status", ignore = true)
    Environment toModel(SaloInput.EnvironmentInput input);

    @Mapping(target = "cloudProfile", ignore = true)
    @Mapping(target = "jxBotUsername", ignore = true)
    @Mapping(target = "repository", ignore = true)
    Cluster toModel(SaloInput.EnvironmentInput.ClusterInput input);

    @Mapping(target = "repository", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "namespace", ignore = true)
    @Mapping(target = "gitUrl", ignore = true)
    @Mapping(target = "gitServer", ignore = true)
    @Mapping(target = "gitKind", ignore = true)
    EnvironmentConfig toModel(SaloInput.EnvironmentInput.ConfigInput input);
}
