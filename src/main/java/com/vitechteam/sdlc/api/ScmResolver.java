package com.vitechteam.sdlc.api;

import com.vitechteam.sdlc.env.DefaultSaloService;
import com.vitechteam.sdlc.env.SaloService;
import com.vitechteam.sdlc.scm.Scm;
import com.vitechteam.sdlc.scm.ScmProvider;
import com.vitechteam.sdlc.scm.github.CustomGithubClient;
import com.vitechteam.sdlc.scm.github.GitHubScm;
import lombok.SneakyThrows;
import org.kohsuke.github.GitHub;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.security.Principal;

@Service
public class ScmResolver {

  static class ScmProviderNotFound extends RuntimeException {
  }

  @SneakyThrows
  public Scm resolve(@Nonnull ScmProvider scm, String token) {
    if (scm == ScmProvider.GITHUB) {
      return new GitHubScm(
        GitHub.connectUsingOAuth(token),
        new CustomGithubClient(token)
      );
    }
    throw new ScmProviderNotFound();
  }

  @Nonnull
  public Scm resolve(@Nonnull Principal principal) {
    if (principal instanceof BearerTokenAuthentication bearer) {
      final OAuth2AccessToken token = bearer.getToken();
      final ScmProvider scm = ScmProvider.valueOf(bearer.getTokenAttributes().get("scm").toString());
      return resolve(scm, token.getTokenValue());
    }

    throw new UnknownScm("unknown scm provider");
  }

  @Nonnull
  public SaloService getSaloService(@Nonnull Principal principal) {
    return new DefaultSaloService(this.resolve(principal));
  }

  public static class UnknownScm extends RuntimeException {
    public UnknownScm(String message) {
      super(message);
    }
  }

}
