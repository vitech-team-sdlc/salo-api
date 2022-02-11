package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.env.model.Salo;

import java.util.Collection;
import java.util.Optional;

public interface SaloService {

    Salo save(Salo salo);

    void applyInfrastructure(Salo salo);

    void destroyInfrastructure(Salo salo);

    Collection<Salo> findByOrganization(String organization);

    Optional<Salo> findByNameAndOrg(String saloName, String organization);

    Optional<Salo> findStatusByNameAndOrg(String name, String organization);
}
