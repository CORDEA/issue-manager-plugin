package jp.cordea.issuemanager;

import lombok.NonNull;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Yoshihiro Tanaka on 2016/09/30.
 */
public class GitHubHelper {

    private String apiUrl;

    GitHubHelper(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @NonNull
    GHRepository getRepository(String repoName) throws RepositoryNotFoundException {
        String token = Configuration.get().getAccessToken();

        if (token == null || token.isEmpty()) {
            throw new RepositoryNotFoundException("Token is empty.");
        }
        GHRepository repository;
        try {
            GitHub gitHub;
            if (apiUrl == null || apiUrl.isEmpty()) {
                gitHub = GitHub.connectUsingOAuth(token);
            } else {
                gitHub = GitHub.connectToEnterprise(apiUrl, token);
            }
            repository = gitHub.getRepository(repoName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RepositoryNotFoundException(e.getMessage());
        }
        return repository;
    }

    String getRepoName(String gitUrl) throws RepositoryNotFoundException {
        try {
            if (apiUrl != null && !apiUrl.isEmpty()) {
                String host = new URL(apiUrl).getHost();
                if (!gitUrl.contains(host)) {
                    throw new RepositoryNotFoundException("Host of the configured api url and git url does not match.");
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RepositoryNotFoundException(e.getMessage());
        }

        String pattern = ".+[/:]([\\w-]+/[\\w-]+)\\.git";
        Matcher matcher = Pattern.compile(pattern).matcher(gitUrl);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new RepositoryNotFoundException("Failed to get the repository name.");
    }

    int getPullRequestNumber(String branch) throws RepositoryNotFoundException {
        String pattern = "[a-zA-Z0-9]+/pr/([0-9]+)/[a-zA-Z0-9]+";
        Matcher matcher = Pattern.compile(pattern).matcher(branch);
        if (matcher.matches()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        throw new RepositoryNotFoundException("Failed to get the Pull request number.");
    }

    static boolean validUser(String user) {
        return user.matches("[a-zA-Z0-9]+[a-zA-Z0-9-]*[a-zA-Z0-9]+|[a-zA-Z0-9]");
    }

    static String[] parseExpandableTextBoxString(String string) {
        if (string == null || string.isEmpty()) {
            return new String[]{};
        }
        String[] strings;
        if (string.contains("\n")) {
            strings = string.split("\n");
        } else {
            strings = string.split(" ");
        }
        List<String> result = new ArrayList<>();
        for (String name : strings) {
            if (!name.isEmpty()) {
                result.add(name);
            }
        }
        return result.toArray(new String[result.size()]);
    }

}
