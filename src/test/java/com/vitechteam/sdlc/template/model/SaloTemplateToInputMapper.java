package com.vitechteam.sdlc.template.model;

import com.vitechteam.sdlc.env.model.rest.SaloInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * README: Test mapper to guarantee the {@link SaloTemplate} fits the {@link SaloInput} models.
 * Only allowed ignorance of {@link SaloInput} properties which must appended to the {@link SaloTemplate}.
 */
@Mapper
public interface SaloTemplateToInputMapper {

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "domain", ignore = true)
    @Mapping(target = "cloudProvider", ignore = true)
    SaloInput mapSalo(SaloTemplate template);

    @Mapping(target = "cloudProviderClientId", ignore = true)
    @Mapping(target = "cloudProviderSecret", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "domainOwner", ignore = true)
    SaloInput.EnvironmentInput.ClusterInput mapCluster(SaloTemplate.EnvironmentTemplate.ClusterTemplate template);

}
