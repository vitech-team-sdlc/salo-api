package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.SaloTestHelper;
import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.scm.github.CustomGithubClient;
import com.vitechteam.sdlc.scm.github.GitHubScm;
import com.vitechteam.sdlc.testConfig.MediumTest;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Collection;


@SpringBootTest
class DefaultSaloServiceTest implements SaloTestHelper {

    final String saloName = "mediumtest";

    @Value("${ghTestToken}")
    private String ghTestToken;

    private GitHubScm gitHubScm;
    private GitHub gitHub;

    @BeforeEach
    void before() throws IOException {
        if (this.gitHub == null) {
            final String ghToken = ghTestToken;
            this.gitHub = GitHub.connectUsingOAuth(ghToken);
            this.gitHubScm = new GitHubScm(
                    gitHub,
                    new CustomGithubClient(ghToken)
            );
        }
    }

    @AfterEach
    void afterAll() {
        dropRepositories(gitHub, saloName);
    }

    @MediumTest
    void testSave() throws IOException {
        dropRepositories(gitHub, saloName);

        final DefaultSaloService defaultSaloService = new DefaultSaloService(gitHubScm);

        final Salo salo = defaultSaloService.save(newDummySalo(saloName));
        Assertions.assertNotNull(salo);

        final String organization = "vitech-team-sdlc";
        final Collection<Salo> salos = defaultSaloService.findByOrganization(organization);

        Assertions.assertEquals(1, salos.size(), "only one salo expected after creation");
        Assertions.assertTrue(
                defaultSaloService.findByNameAndOrg(salo.name(), organization).isPresent(),
                "Expected to found salo with name" + salo.name()
        );
    }

    private void dropRepositories(GitHub gitHub, String salo) {
        try {
            gitHub.getRepository("vitech-team-sdlc/infra-" + salo + "-DEV").delete();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
        try {
            gitHub.getRepository("vitech-team-sdlc/env-" + salo + "-DEV").delete();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

}
