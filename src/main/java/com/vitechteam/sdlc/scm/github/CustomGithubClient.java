package com.vitechteam.sdlc.scm.github;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.interfaces.Box;
import com.goterl.lazysodium.interfaces.Helpers;
import com.goterl.lazysodium.utils.Key;
import com.vitechteam.sdlc.scm.Repository;
import com.vitechteam.sdlc.scm.Secret;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.kohsuke.github.GitHub;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;
import java.util.Optional;

@AllArgsConstructor
public class CustomGithubClient {

    private final String accessToken;

    private final GitHub gitHub;
    private final RestTemplate restTemplate;

    @SneakyThrows
    public CustomGithubClient(String accessToken) {
        this.gitHub = GitHub.connectUsingOAuth(accessToken);
        this.accessToken = accessToken;
        this.restTemplate = new RestTemplateBuilder()
                .defaultHeader("Authorization", "token " + accessToken)
                .rootUri(gitHub.getApiUrl())
                .build();
    }

    public void createSecret(Secret secret, Repository repo) {
        final PubKey pubKey = getPubKey(repo);
        final String encryptedVal = encrypt(pubKey, secret.value());
        this.restTemplate.put(
                "/repos/{owner}/{repo}/actions/secrets/{secret}",
                new EncryptedSecret(encryptedVal, pubKey.keyId()),
                repo.organization(), repo.name(), secret.key()
        );
    }

    @Nonnull
    private PubKey getPubKey(Repository repo) {
        final PubKey pubKey = this.restTemplate.getForObject(
                "/repos/{owner}/{repo}/actions/secrets/public-key",
                PubKey.class,
                repo.organization(), repo.name()
        );
        return Optional.ofNullable(pubKey).orElseThrow(() -> new IllegalStateException("can't fetch public key"));
    }

    private record EncryptedSecret(
            String encrypted_value,
            String key_id
    ) {
    }

    @Nonnull
    @SneakyThrows
    private String encrypt(PubKey pubKey, String value) {
        final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());
        final byte[] encryptionKey = java.util.Base64.getDecoder().decode(pubKey.key().getBytes());
        final String secretEncrypted = ((Box.Lazy) lazySodium).cryptoBoxSealEasy(value, Key.fromBytes(encryptionKey));
        final byte[] secretByteArray = ((Helpers.Lazy) lazySodium).sodiumHex2Bin(secretEncrypted);
        return new String(java.util.Base64.getEncoder().encode(secretByteArray));
    }

    public String getAccessToken() {
        return accessToken;
    }
}
