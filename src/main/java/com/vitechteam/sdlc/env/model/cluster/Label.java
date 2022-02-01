package com.vitechteam.sdlc.env.model.cluster;

import java.util.Collection;
import java.util.List;

public record Label(String key, String value) {

    public static Collection<Label> of(String k, String v) {
        return List.of(new Label(k, v));
    }
}
