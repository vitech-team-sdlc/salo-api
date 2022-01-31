package com.vitechteam.sdlc.scm;

import java.util.List;

public record User(String id, String avatarUrl, List<Organization> organizations) {
}
