package org.jenkinsci.plugins.displayurlapi;

import hudson.EnvVars;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.assertEquals;

public class DisplayURLProviderTest {

    @Rule
    public JenkinsRuleWithLocalPort rule = new JenkinsRuleWithLocalPort();

    @Test
    public void urls() throws Exception {
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject project = (FreeStyleProject) folder.createProject(rule.jenkins.getDescriptorByType(FreeStyleProject.DescriptorImpl.class), "my job", false);
        Run<?, ?> run = project.scheduleBuild2(0).get();

        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect", DisplayURLProvider.get().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/redirect", DisplayURLProvider.get().getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=changes", DisplayURLProvider.get().getChangesURL(run));

        EnvVars environment = run.getEnvironment();
        assertEquals(DisplayURLProvider.get().getRunURL(run), environment.get("RUN_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getRunURL(run), environment.get("BUILD_URL"));
        assertEquals(DisplayURLProvider.get().getChangesURL(run), environment.get("RUN_CHANGES_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getJobURL(project), environment.get("JOB_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getJobURL(project), environment.get("JOB_URL"));
    }
}
