package org.jenkinsci.plugins.displayurlapi;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Run;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.assertEquals;

public class DisplayURLProviderTest {

    @Rule
    public JenkinsRuleWithLocalPort rule = new JenkinsRuleWithLocalPort();
    private Run<?, ?> run;

    private Job<?, ?> job;

    @Test
    public void urls() throws Exception {
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject project = (FreeStyleProject) folder.createProject(FreeStyleProject.DESCRIPTOR, "my job", false);
        job = project;
        run = project.scheduleBuild2(0).get();

        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/", DisplayURLProvider.get().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/", DisplayURLProvider.get().getJobURL(job));
        assertEquals(root + "job/my%20folder/job/my%20job/changes", DisplayURLProvider.get().getChangesURL(run));
    }

    class JenkinsRuleWithLocalPort extends JenkinsRule {
        public int getLocalPort() {
            return this.localPort;
        }
    }
}
