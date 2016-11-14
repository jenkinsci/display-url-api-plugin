package org.jenkinsci.plugins.displayurlapi;

import com.google.inject.Inject;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DisplayURLProviderTest {

    @Rule
    public JenkinsRuleWithLocalPort rule = new JenkinsRuleWithLocalPort();

    @Test
    public void urls() throws Exception {
        FreeStyleProject project = createTestProject();
        Run<?, ?> run = project.scheduleBuild2(0).get();

        this.testUrls(project, run, "");
    }

    @Test
    public void testGetTestURL() throws Exception {
        FreeStyleProject project = createTestProject();
        Run<?, ?> run = project.scheduleBuild2(0).get();

        this.testGetTestUrl(run, "");
    }

    @Test
    public void extendPlugin() throws Exception {
        FreeStyleProject project = createTestProject();
        Run<?, ?> run = project.scheduleBuild2(0).get();

        this.testUrls(project, run, AnotherDisplayURLProvider.EXTRA_CONTENT_IN_URL);
        this.testGetTestUrl(run, AnotherDisplayURLProvider.EXTRA_CONTENT_IN_URL);
    }

    private FreeStyleProject createTestProject() throws Exception {
        MockFolder folder = rule.createFolder("my folder");
        return (FreeStyleProject) folder.createProject(FreeStyleProject.DESCRIPTOR, "my job", false);
    }

    private void testUrls(FreeStyleProject project, Run run, String extraContentInUrl) throws Exception {
        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/" + extraContentInUrl, DisplayURLProvider.get().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/" + extraContentInUrl, DisplayURLProvider.get().getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/changes" + extraContentInUrl, DisplayURLProvider.get().getChangesURL(run));
    }

    private void testGetTestUrl(Run run, String extraContentInUrl) throws Exception {
        TestResult result = mock(TestResult.class);

        AbstractTestResultAction action = mock(AbstractTestResultAction.class);
        when(action.getUrlName()).thenReturn("action");

        when(result.getRun()).thenReturn(run);
        when(result.getTestResultAction()).thenReturn(action);
        when(result.getUrl()).thenReturn("/some id with spaces");

        String testUrl = DisplayURLProvider.get().getTestUrl(result);
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/job/my%20folder/job/my%20job/1/action/some%20id%20with%20spaces" + extraContentInUrl, testUrl);
    }

    class JenkinsRuleWithLocalPort extends JenkinsRule {
        public int getLocalPort() {
            return this.localPort;
        }
    }

    @TestExtension("extendPlugin")
    public static class AnotherDisplayURLProvider extends DisplayURLProvider {

        public static final String EXTRA_CONTENT_IN_URL = "another";

        @Inject
        private ClassicDisplayURLProvider defaultProvider;

        @Override
        public String getRunURL(Run<?, ?> run) {
            return defaultProvider.getRunURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @Override
        public String getChangesURL(Run<?, ?> run) {
            return defaultProvider.getChangesURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @Override
        public String getJobURL(Job<?, ?> project) {
            return defaultProvider.getJobURL(project) + EXTRA_CONTENT_IN_URL;
        }

        @Override
        public String getTestUrl(hudson.tasks.test.TestResult result) {
            return defaultProvider.getTestUrl(result) + EXTRA_CONTENT_IN_URL;
        }
    }
}
