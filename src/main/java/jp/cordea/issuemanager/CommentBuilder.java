package jp.cordea.issuemanager;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;
import hudson.tasks.*;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import lombok.Getter;
import net.sf.json.JSONObject;
import org.kohsuke.github.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;

public class CommentBuilder extends Recorder implements SimpleBuildStep {

    @Getter
    private final String message;

    @Getter
    private final String path;

    @Getter
    private final String number;

    @Getter
    private final boolean isPullRequestBuilder;

    @DataBoundConstructor
    public CommentBuilder(String message, String path, String number, boolean isPullRequestBuilder) {
        this.message = message;
        this.path = path;
        this.number = number;
        this.isPullRequestBuilder = isPullRequestBuilder;
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

        int num;
        if (isPullRequestBuilder) {
            String branch = run.getEnvironment(taskListener).get("GIT_BRANCH");
            try {
                num = helper.getPullRequestNumber(branch);
            } catch (RepositoryNotFoundException e) {
                taskListener.error(e.getMessage());
                return;
            }
        } else {
            if (number == null || number.isEmpty()) {
                taskListener.error("Path not found. Please set file path or message.");
                return;
            }
            num = Integer.parseInt(number);
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

        try {
            Issue issue = Issue.getIssue(repository, num);
            if (issue != null) {
                issue.comment(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            taskListener.error("Failed to post a comment.");
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckMessage(@QueryParameter String value) throws IOException, ServletException {
            return FormValidation.ok();
        }

        public FormValidation doCheckPath(@QueryParameter String value) throws IOException, ServletException {
            return FormValidation.ok();
        }

        public FormValidation doCheckNumber(@QueryParameter String value) throws IOException, ServletException {
            if (value.isEmpty()) {
                return FormValidation.ok();
            }
            try {
                int ignore = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return FormValidation.error("Please set a number.");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        public String getDisplayName() {
            return "Comment to Issue or Pull request";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            return super.configure(req,formData);
        }
    }
}

