package jp.cordea.issuemanager;

import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by Yoshihiro Tanaka on 2016/09/30.
 */
public class Issue {

    private GHIssue issue;
    private GHPullRequest pullRequest;

    public Issue(GHIssue issue) {
        this.issue = issue;
    }

    public Issue(GHPullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }

    static Issue getIssue(GHRepository repository, int number) throws IOException {
        GHIssue issue = repository.getIssue(number);
        if (issue != null) {
            return new Issue(issue);
        }
        GHPullRequest pullRequest = repository.getPullRequest(number);
        if (pullRequest != null) {
            return new Issue(pullRequest);
        }
        return null;
    }

    static GHIssue open(GHRepository repository, String title, String message, String[] assignees) {
        try {
            GHIssueBuilder builder = repository.createIssue(title).body(message);
            for (String assignee : assignees) {
                builder.assignee(assignee);
            }
            return builder.create();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    boolean reopen() {
        try {
            if (issue == null) {
                if (pullRequest == null) {
                    return false;
                }
                pullRequest.reopen();
            } else {
                issue.reopen();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean close() {
        try {
            if (issue == null) {
                if (pullRequest == null) {
                    return false;
                }
                pullRequest.close();
            } else {
                issue.close();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    GHIssueComment comment(String message) {
        try {
            if (issue != null) {
                return issue.comment(message);
            }
            return pullRequest.comment(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
