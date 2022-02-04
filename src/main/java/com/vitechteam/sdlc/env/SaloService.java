package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.env.model.Salo;

import java.util.Collection;

public interface SaloService {

    Salo save(Salo salo);

    Collection<Salo> findByOrganization(String organization);

    Salo findByNameAndOrg(String saloName, String organization);

    Salo findStatusByNameAndOrg(String name, String organization);
}
