package org.jenkinsci.plugins.displayurlapi.actions;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.displayurlapi.JenkinsRuleWithLocalPort;
import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.MockFolder;

public abstract class AbstractActionRedirectTest {
    protected Job job;
    protected Run<?, ?> run;
    protected DisplayURLProvider provider;

    @Rule
    public JenkinsRuleWithLocalPort rule = new JenkinsRuleWithLocalPort();

    @Before
    public void createJobAndRun() throws Exception {
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject job = (FreeStyleProject) folder.createProject(FreeStyleProject.DESCRIPTOR, "my job", false);
        this.job = job;
        this.run = job.scheduleBuild2(0).get();
        provider = DisplayURLProvider.get();
    }

    protected abstract DisplayURLProvider getRedirectedProvider();
}
