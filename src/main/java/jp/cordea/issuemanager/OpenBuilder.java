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
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import lombok.Getter;
import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Created by Yoshihiro Tanaka on 2016/09/30.
 */
public class OpenBuilder extends Recorder implements SimpleBuildStep {

    @Getter
    private final String message;

    @Getter
    private final String path;

    @Getter
    private final String title;

    @Getter
    private final String assignees;

    @DataBoundConstructor
    public OpenBuilder(String message, String path, String title, String assignees) {
        this.message = message;
        this.path = path;
        this.title = title;
        this.assignees = assignees;
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

        String message = this.message;
        if (message == null || message.isEmpty()) {
            taskListener.getLogger().println("Message is not registered. Checking the file path...");
            String path = run.getEnvironment(taskListener).expand(this.path);
            if (path == null || path.isEmpty()) {
                taskListener.error("Path not found. Please set file path or message.");
                return;
            }
            taskListener.getLogger().println("Reading " + path + "...");
            FilePath readFilePath = filePath.child(path);
            message = readFilePath.readToString();
        }

        if (message == null || message.isEmpty()) {
            taskListener.error("Message is empty.");
            return;
        }

        String[] strings = GitHubHelper.parseExpandableTextBoxString(assignees);
        Issue.open(repository, title, message, strings);
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

        public FormValidation doCheckTitle(@QueryParameter String value) throws IOException, ServletException {
            if (value.isEmpty()) {
                return FormValidation.error("Please set a title.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckAssignees(@QueryParameter String value) throws IOException, ServletException {
            if (value.isEmpty()) {
                return FormValidation.ok();
            }
            for (String string : GitHubHelper.parseExpandableTextBoxString(value)) {
                if (!GitHubHelper.validUser(string)) {
                    return FormValidation.error("User name is incorrect: " + string);
                }
            }
            return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return "Open Issue or Pull request";
        }

    }

}
