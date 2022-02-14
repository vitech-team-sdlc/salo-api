package com.vitechteam.sdlc.env;

import com.vitechteam.sdlc.TestFixtures;
import com.vitechteam.sdlc.env.model.Salo;
import com.vitechteam.sdlc.scm.PipelineStatus;
import com.vitechteam.sdlc.scm.Repository;
import com.vitechteam.sdlc.scm.github.CustomGithubClient;
import com.vitechteam.sdlc.scm.github.GitHubScm;
import com.vitechteam.sdlc.testConfig.LargeTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIf;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class DefaultSaloServiceTest implements TestFixtures {

    private static boolean saloCreatedSuccessfully = false;

    @Value("${largeTest.github.testToken}")
    private String ghTestToken;

    @Value("${largeTest.github.organization}")
    private String ghOrganization;

    @Value("${largeTest.aws.clientId}")
    private String awsClientId;

    @Value("${largeTest.aws.secret}")
    private String awsSecret;

    private GitHubScm gitHubScm;
    private GitHub gitHub;
    private DefaultSaloService defaultSaloService;

    private String name;
    private Salo saloTemplate;

    private Salo salo;
    private boolean infraDestroyed;

    @BeforeAll
    public void beforeAll() throws IOException {
        gitHub = GitHub.connectUsingOAuth(ghTestToken);
        gitHubScm = new GitHubScm(gitHub, new CustomGithubClient(ghTestToken));
        defaultSaloService = new DefaultSaloService(gitHubScm);

        name = "test-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        saloTemplate = newSalo(name, ghOrganization, awsClientId, awsSecret);
    }

    @LargeTest
    @DisplayName("Create a new Salo")
    void createNewSaloInstance() {
        // create a new salo
        final Salo savedSalo = defaultSaloService.save(saloTemplate);

        // validate just created instance
        final Optional<Salo> foundSalo = defaultSaloService.findByNameAndOrg(savedSalo.name(), ghOrganization);
        assertThat(foundSalo)
                .as("expected to find salo with name [%s]", name)
                .isPresent();

        // remember in context for next steps
        salo = foundSalo.get();

        // mark test as passed
        saloCreatedSuccessfully = true;
    }

    @Nested
    @EnabledIf(value = "saloCreatedSuccessfully")
    @TestInstance(Lifecycle.PER_CLASS)
    public class InfraCreationTest {

        private static boolean infraCreatedSuccessfully = false;

        private Repository infraRepository;

        public static boolean saloCreatedSuccessfully() {
            return saloCreatedSuccessfully;
        }

        @BeforeAll
        public void beforeAll() {
            infraRepository = salo.findDevEnvironment().cluster().getRepository();
        }

        @LargeTest
        @DisplayName("Apply infrastructure")
        void applyInfrastructure() {
            // trigger infrastructure deploy
            defaultSaloService.applyInfrastructure(salo);

            long executionId = waitForExecutionId("apply infrastructure");

            waitPipelineCompletion(executionId, "apply infrastructure");

            // mark test as passed
            infraCreatedSuccessfully = true;
        }

        private long waitForExecutionId(String label) {
            // wait until pipeline starts
            waitAtMost(1, TimeUnit.MINUTES)
                    .alias("waiting until [%s] pipeline starts".formatted(label))
                    .pollInterval(1, TimeUnit.SECONDS)
                    .await()
                    .until(() -> gitHubScm
                            .findLatestInfraPipelineStatus(infraRepository)
                            .filter(status -> Set.of("QUEUED", "IN_PROGRESS").contains(status.status()))
                            .isPresent());

            // wait until AWS environment gets rolled up and GIT infra repository updated
            final Optional<PipelineStatus> pipelineStatus = gitHubScm.findLatestInfraPipelineStatus(infraRepository);
            assertThat(pipelineStatus)
                    .as("infra pipeline should be triggered in [%s] repo", infraRepository.fullName())
                    .isPresent();

            return pipelineStatus.get().id();
        }

        private void waitPipelineCompletion(long executionId, String label) {
            waitAtMost(1, TimeUnit.HOURS)
                    .alias("waiting until [%s] pipeline successfully completes".formatted(label))
                    .pollInterval(5, TimeUnit.SECONDS)
                    .await()
                    .until(() -> isPipelineSuccessfullyCompleted(executionId));
        }

        private Boolean isPipelineSuccessfullyCompleted(long executionId) {
            final PipelineStatus status = gitHubScm.getPipelineExecutionStatus(infraRepository, executionId);

            switch (status.status()) {
                case "QUEUED", "IN_PROGRESS" -> {
                    return false;
                }
                case "COMPLETED" -> {
                    assertThat(status.conclusion())
                            .as("pipeline execution conclusion is not successful")
                            .isEqualTo("SUCCESS");
                    return true;
                }
                default -> {
                    fail("Unexpected pipeline execution status: " + status);
                    return false;
                }
            }
        }

        @Nested
        @EnabledIf(value = "infraCreatedSuccessfully")
        @TestInstance(Lifecycle.PER_CLASS)
        public class EnvironmentCreationTest {

            private static boolean envCreatedSuccessfully = false;

            public static boolean infraCreatedSuccessfully() {
                return infraCreatedSuccessfully;
            }

            @Disabled("TODO: sdlc-helper app is required to complete the step")
            @LargeTest
            @DisplayName("Boot k8s Environment")
            @Timeout(value = 20, unit = TimeUnit.MINUTES)
            void bootK8sEnvironment() {
                // TODO: monitor jx-boot job progress and pods statuses, GIT env repository updated accordingly
                //       ...

                envCreatedSuccessfully = true;
            }

            @Nested
            @TestInstance(Lifecycle.PER_CLASS)
            public class InfraDestructionTest {

                @LargeTest
                @DisplayName("Destroy infrastructure")
                void destroyInfrastructure() {
                    defaultSaloService.destroyInfrastructure(salo);

                    long executionId = waitForExecutionId("destroy infrastructure");

                    waitPipelineCompletion(executionId, "destroy infrastructure");

                    // only if test successfully destroyed the infrastructure - allow dropping repositories
                    infraDestroyed = true;
                }

                @AfterAll
                public void afterAll() {
                    if (infraDestroyed) {
                        // TODO: remove S3 bucket with tf state
                    }
                }
            }
        }
    }

    @AfterAll
    public void afterAll() {
        if (infraDestroyed) {
            // TODO migrate to GH client
            dropRepository("infra");
            dropRepository("env");
        } else {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                    "        !!! SOMETHING GOES WRONG !!!\n" +
                    "   Test didn't manage to drop repositories,\n" +
                    "   please review the [" + ghOrganization + "/" + name + "]\n" +
                    "   installation status and perform manual clean up\n" +
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void dropRepository(String repoKind) {
        try {
            gitHub.getRepository("%s/%s-%s-dev".formatted(ghOrganization, repoKind, name)).delete();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
