# Getting Started

## Initial configuration

For local setup create new spring config file called  `application-private.yaml` with next content for [PAT](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) access:

### Personal access token auth
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        opaquetoken:
          introspection-uri: https://api.github.com/user
          client-id: token
          client-secret: ghp_REPLACE_ME
```

### GitHub app auth
If you are wanna use real setup based on GitHub app use next configs:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        opaquetoken:
          introspection-uri: https://api.github.com/applications/Iv1.94657f4b86f3738d/token
          client-id: Iv1.94657f4b86f3738d
          client-secret: REPLACE_ME
```

or just init env variable `GH_CLIENT_SECRET`.

## App Execution
If you're using IntelliJ IDEA, you should see preconfigured configuration: `SaloApiApplication[DEV+PRIVATE]` 

# Docs
Technical documentation could be found [here](docs/docs.md)

# UI 
Repository: https://github.com/vitech-team-sdlc/sdlc-ui
Mockups: https://www.figma.com/file/BmnoCSSRYaPxCsqd1IJTCa/Salo-(SDLC)?node-id=0%3A1
Initial dev mockups: https://whimsical.com/sdlc-9iJvu6pNAXzUQBYYR61qAM
