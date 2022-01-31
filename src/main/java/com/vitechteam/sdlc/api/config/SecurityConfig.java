
package com.vitechteam.sdlc.api.config;

import com.vitechteam.sdlc.api.config.introspector.GitHubNimbusOpaqueTokenIntrospector;
import com.vitechteam.sdlc.api.config.introspector.GitHubPersonalTokenIntrospector;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

@Configuration
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Bean
  @Profile("!dev")
  public OpaqueTokenIntrospector oauthOpaqueTokenIntrospector(
    OAuth2ResourceServerProperties oAuth2ResourceServerProperties
  ) {
    return new GitHubNimbusOpaqueTokenIntrospector(oAuth2ResourceServerProperties);
  }

  @Bean
  @Profile("dev")
  public OpaqueTokenIntrospector personalOpaqueTokenIntrospector(
    OAuth2ResourceServerProperties oAuth2ResourceServerProperties
  ) {
    return new GitHubPersonalTokenIntrospector(oAuth2ResourceServerProperties);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http
      .csrf().disable()
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
        .authorizeRequests()
          .antMatchers("/oauth/**").permitAll()
          .anyRequest().authenticated()
      .and()
      .oauth2ResourceServer()
      .opaqueToken();
    // @formatter:on
  }


}
