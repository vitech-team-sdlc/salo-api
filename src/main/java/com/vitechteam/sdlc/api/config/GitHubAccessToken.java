package com.vitechteam.sdlc.api.config;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class GitHubAccessToken {
    String access_token;
    int expires_in;
    String refresh_token;
    int refresh_token_expires_in;
    String scope;
    String token_type;
    @With
    Boolean active;
}
