package org.jenkinsci.plugins.displayurlapi;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DisplayURLProviderTest {

    @Rule
    public JenkinsRuleWithLocalPort rule = new JenkinsRuleWithLocalPort();

    @Test
    public void urls() throws Exception {
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject project = (FreeStyleProject) folder.createProject(FreeStyleProject.DESCRIPTOR, "my job", false);
        Run<?, ?> run = project.scheduleBuild2(0).get();

        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/", DisplayURLProvider.get().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/", DisplayURLProvider.get().getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/changes", DisplayURLProvider.get().getChangesURL(run));
    }

    @Test
    public void testGetTestURL() throws Exception {
        TestResult result = mock(TestResult.class);

        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject project = (FreeStyleProject) folder.createProject(FreeStyleProject.DESCRIPTOR, "my job", false);
        Run<?, ?> run = project.scheduleBuild2(0).get();

        AbstractTestResultAction action = mock(AbstractTestResultAction.class);
        when(action.getUrlName()).thenReturn("action");

        when(result.getRun()).thenReturn((Run)run);
        when(result.getTestResultAction()).thenReturn(action);
        when(result.getUrl()).thenReturn("/some id with spaces");

        String testUrl = DisplayURLProvider.get().getTestUrl(result);
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/job/my%20folder/job/my%20job/1/action/some%20id%20with%20spaces", testUrl);
    }

    class JenkinsRuleWithLocalPort extends JenkinsRule {
        public int getLocalPort() {
            return this.localPort;
        }
    }
}
