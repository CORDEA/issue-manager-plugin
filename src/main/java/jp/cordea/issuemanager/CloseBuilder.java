package jp.cordea.issuemanager;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import lombok.Getter;
import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by Yoshihiro Tanaka on 2016/09/30.
 */
public class CloseBuilder extends Recorder implements SimpleBuildStep {

    @Getter
    private final String number;

    @DataBoundConstructor
    public CloseBuilder(String number) {
        this.number = number;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        String gitUrl = run.getEnvironment(taskListener).get("GIT_URL");
        if (gitUrl == null || gitUrl.isEmpty()) {
            taskListener.error("GIT_URL is empty.");
            return;
        }
        GitHubHelper helper = new GitHubHelper(Configuration.get().getApiUrl());
        GHRepository repository;
        try {
            String repo = helper.getRepoName(gitUrl);
            repository = helper.getRepository(repo);
        } catch (RepositoryNotFoundException e) {
            taskListener.error(e.getMessage());
            return;
        }

        int num = Integer.parseInt(number);

        try {
            Issue issue = Issue.getIssue(repository, num);
            if (issue != null) {
                issue.close();
            }
        } catch (IOException e) {
            taskListener.error("Failed to close the issue or pull request.");
            e.printStackTrace();
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Close Issue or Pull request";
        }

    }

}
