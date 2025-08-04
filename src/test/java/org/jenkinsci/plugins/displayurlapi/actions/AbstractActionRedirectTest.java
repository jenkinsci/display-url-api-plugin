package org.jenkinsci.plugins.displayurlapi.actions;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.ArtifactArchiver;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.jupiter.api.BeforeEach;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.IOException;

@WithJenkins
abstract class AbstractActionRedirectTest {
    protected Job job;
    protected Run<?, ?> run;
    protected DisplayURLProvider provider;

    protected JenkinsRule rule;

    @BeforeEach
    void setUp(JenkinsRule r) throws Exception {
        rule = r;

        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject job = (FreeStyleProject) folder.createProject(rule.jenkins.getDescriptorByType(FreeStyleProject.DescriptorImpl.class), "my job", false);
        job.getBuildersList().add(new CreateArtifact());
        job.getPublishersList().add(new ArtifactArchiver("f"));
        this.job = job;
        this.run = job.scheduleBuild2(0).get();
        provider = DisplayURLProvider.get();
    }

    static class CreateArtifact extends TestBuilder {
        public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
            build.getWorkspace().child("f").write("content", "UTF-8");
            return true;
        }
    }

    protected abstract DisplayURLProvider getRedirectedProvider();
}
