package com.vitechteam.sdlc.scm;

public record UpdateInfrastructureParams(String region, boolean destroy, boolean apply) {
}
