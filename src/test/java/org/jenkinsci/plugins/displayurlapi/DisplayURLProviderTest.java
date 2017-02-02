package org.jenkinsci.plugins.displayurlapi;

import hudson.EnvVars;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
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
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect", DisplayURLProvider.get().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/redirect", DisplayURLProvider.get().getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=changes", DisplayURLProvider.get().getChangesURL(run));

        EnvVars environment = run.getEnvironment();
        assertEquals(DisplayURLProvider.get().getRunURL(run), environment.get("RUN_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getChangesURL(run), environment.get("RUN_CHANGES_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getJobURL(project), environment.get("JOB_DISPLAY_URL"));
    }

    @Test
    public void testGetTestURL() throws Exception {

        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject project = (FreeStyleProject) folder.createProject(FreeStyleProject.DESCRIPTOR, "my job", false);
        Run<?, ?> run = project.scheduleBuild2(0).get();
        MockTestResult result = new MockTestResult(run);

        AbstractTestResultAction action = mock(AbstractTestResultAction.class);
        when(action.getUrlName()).thenReturn("action");
        when(action.findCorrespondingResult(anyString())).thenReturn(result);

        String testUrl = DisplayURLProvider.get().getTestUrl(result);

        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/job/my%20folder/job/my%20job/1/display/redirect?page=test&id=some%20id%20with%20spaces", testUrl);
    }

    class MockTestResult extends TestResult {

        private final Run<?, ?> owner;

        public MockTestResult(Run<?, ?> owner) {
            this.owner = owner;
        }

        @Override
        public String getName() {
            return "some id with spaces";
        }

        @Override
        public Run<?, ?> getRun() {
            return owner;
        }

        @Override
        public TestObject getParent() {
            return null;
        }

        @Override
        public TestResult findCorrespondingResult(String id) {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }
    }
}
