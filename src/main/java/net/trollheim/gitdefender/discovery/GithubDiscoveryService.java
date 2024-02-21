package net.trollheim.gitdefender.discovery;

import net.trollheim.gitdefender.Constants;
import net.trollheim.gitdefender.actions.ActionConfig;
import net.trollheim.gitdefender.cmd.ActionConfigProperties;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.function.Function;

public class GithubDiscoveryService {

    private final GitHub github;



    public GithubDiscoveryService(ActionConfig properties) {
        String username = properties.get(Constants.GIT_USERNAME_FIELD);
        String password = properties.get(Constants.GIT_PASSWORD_FIELD);
        try {
            this.github = new GitHubBuilder().withPassword(username, password).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Collection<GHRepository> getRepos(String owner, boolean isOrg) throws IOException {

        if (isOrg){
            return github.getOrganization(owner).getRepositories().values();
        } else {
            return github.getUser(owner).getRepositories().values();
        }
    }


    public <T> T execute(String repoName, Function<GHRepository,T> action){
        try {
            return action.apply(github.getRepository(repoName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
