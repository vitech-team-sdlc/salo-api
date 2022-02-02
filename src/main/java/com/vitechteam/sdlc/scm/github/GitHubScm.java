package com.vitechteam.sdlc.scm.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vitechteam.sdlc.env.model.config.JxRequirements;
import com.vitechteam.sdlc.env.model.tf.TfVars;
import com.vitechteam.sdlc.scm.File;
import com.vitechteam.sdlc.scm.Organization;
import com.vitechteam.sdlc.scm.ScmProvider;
import com.vitechteam.sdlc.scm.Repository;
import com.vitechteam.sdlc.scm.Scm;
import com.vitechteam.sdlc.scm.Secret;
import com.vitechteam.sdlc.scm.UpdateInfrastructureParams;
import com.vitechteam.sdlc.scm.User;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHContentUpdateResponse;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class GitHubScm implements Scm {

    private static final String JX_REQ_FILE_PATH = "jx-requirements.yml";
    private static final String SALO_TF_VARS_FILE_PATH = "salo.auto.tfvars.json";
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

    private final GitHub gitHub;
    private final CustomGithubClient customGithubClient;
    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public GitHubScm(GitHub gitHub, CustomGithubClient customGithubClient) {
        this(
                gitHub,
                customGithubClient,
                new ObjectMapper(new YAMLFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        );
    }

    @Override
    public ScmProvider provider() {
        return ScmProvider.GITHUB;
    }

    @SneakyThrows
    @Override
    public User currentUser() {
        final GHMyself user = gitHub.getMyself();
        return new User(
                user.getLogin(),
                user.getAvatarUrl(),
                toOrganizations(user.getAllOrganizations())
        );
    }

    @SneakyThrows
    @Override
    public User findUser(String id) {
        final GHUser user = gitHub.getUser(id);
        return toUser(user);
    }

    private User toUser(GHUser user) throws IOException {
        return new User(
                user.getLogin(),
                user.getAvatarUrl(),
                toOrganizations(user.getOrganizations())
        );
    }

    @SneakyThrows
    @Override
    public Collection<Organization> findAllOrganizations() {
        return currentUser().organizations();
    }

    @SneakyThrows
    @Override
    public Collection<Repository> findRepositoriesByOrg(@Nonnull String organization) {
        return this.gitHub
                .getMyself()
                .getAllRepositories()
                .values()
                .stream()
                .filter(r -> r.getOwnerName().equals(organization))
                .map(this::toRepository)
                .toList();
    }

    @Override
    @SneakyThrows
    public Repository create(Repository newRepo, Repository template) {
        final GHRepository ghRepository = this.gitHub
                .createRepository(newRepo.name())
                .fromTemplateRepository(template.organization(), template.name())
                .owner(newRepo.organization())
                .private_(true)
                .create();

        return toRepository(ghRepository);
    }

    @Override
    public void createSecret(String key, String value, Repository repository) {
        this.customGithubClient.createSecret(new Secret(key, value), repository);
    }

    @Override
    public String generateAccessToken() {
        return customGithubClient.getAccessToken();
    }

    @SneakyThrows
    @Override
    public JxRequirements getJxRequirements(Repository envRepository) {
        final File file = findFile(new File(JX_REQ_FILE_PATH), envRepository);
        return yamlMapper.readValue(file.content(), JxRequirements.class);
    }

    @SneakyThrows
    @Override
    public JxRequirements updateJxRequirements(JxRequirements requirementsConfig, Repository envRepository) {
        final String content = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requirementsConfig);
        final File file = updateFile(
                new File(
                        content,
                        "",
                        JX_REQ_FILE_PATH
                ),
                envRepository,
                "chore: update jx-requirements"
        );
        return yamlMapper.readValue(file.content(), JxRequirements.class);
    }

    @SneakyThrows
    @Override
    public TfVars getTfVars(Repository repository) {
        final File fileContent = findFile(new File(SALO_TF_VARS_FILE_PATH), repository);
        return jsonMapper.readValue(fileContent.content(), TfVars.class);
    }

    @SneakyThrows
    @Override
    public void updateTfVars(TfVars tfVars, Repository repository) {
        final String content = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tfVars);
        final File fileForUpdate = new File(content, "", SALO_TF_VARS_FILE_PATH);
        updateFile(fileForUpdate, repository, "chore: update salo tf variables");
    }

    @SneakyThrows
    @Override
    public File findFile(File file, Repository repository) {
        final GHContent fileContent = this.gitHub.getRepository(repository.fullName()).getFileContent(file.path(), repository.branch());
        return new File(IOUtils.toString(fileContent.read(), DEFAULT_CHARSET), fileContent.getSha(), file.path());
    }

    @SneakyThrows
    @Override
    public File updateFile(File file, Repository repository, String commitMessage) {
        final GHContent fileContent = this.gitHub.getRepository(repository.fullName()).getFileContent(file.path(), repository.branch());
        final GHContentUpdateResponse update = fileContent.update(file.content(), commitMessage, repository.branch());
        return new File(
                IOUtils.toString(update.getContent().read(), DEFAULT_CHARSET),
                update.getCommit().getSHA1(),
                update.getContent().getPath()
        );
    }

    @SneakyThrows
    @Override
    public void triggerInfrastructureUpdate(Repository repository, UpdateInfrastructureParams params) {
        this.gitHub
                .getRepository(repository.fullName())
                .getWorkflow("main.yml")
                .dispatch(repository.branch(), Map.of(
                        "awsRegion", params.region(),
                        "destroyInfra", Boolean.toString(params.destroy()),
                        "applyInfra", Boolean.toString(params.apply())
                ));
    }

    private Repository toRepository(GHRepository ghRepository) {
        return new Repository(
                ghRepository.getName(),
                ghRepository.getOwnerName(),
                ghRepository.getDefaultBranch(),
                ghRepository.getHttpTransportUrl()
        );
    }

    private List<Organization> toOrganizations(Collection<GHOrganization> myOrganizations) {
        return myOrganizations.stream().map(ghOrg -> new Organization(ghOrg.getLogin())).toList();
    }
}
