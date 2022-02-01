package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.testConfig.MediumTest;
import com.vitechteam.sdlc.SaloTestHelper;
import com.vitechteam.sdlc.scm.github.CustomGithubClient;
import com.vitechteam.sdlc.scm.github.GitHubScm;
import org.junit.jupiter.api.Assertions;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;


@SpringBootTest
@ActiveProfiles("private")
class DefaultSaloServiceTest implements SaloTestHelper {

    @Value("${salo.testing.gh_token}")
    private String ghToken;

    @MediumTest
    void testSave() throws IOException {
        final GitHub gitHub = GitHub.connectUsingOAuth(ghToken);
        final DefaultSaloService defaultSaloService = new DefaultSaloService(
                new GitHubScm(
                        gitHub,
                        new CustomGithubClient(ghToken)
                )
        );

        dropRepositories(gitHub);

        Assertions.assertNotNull(defaultSaloService.save(newDummySalo("test")));
    }

    private void dropRepositories(GitHub gitHub) {
        try {
            gitHub.getRepository("vitech-team-sdlc/infra-test-DEV").delete();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
        try {
            gitHub.getRepository("vitech-team-sdlc/env-test-DEV").delete();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

}
