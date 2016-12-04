package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import javax.servlet.http.HttpServletResponse;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActionRedirectExtendedTest extends AbstractActionRedirectTest {
    @Test
    public void testRedirectForJobURL() throws Exception {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getJobURL(job)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getJobURL(job));
    }

    @Test
    public void testRedirectForRunURL() throws Exception {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getRunURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getRunURL(run));
    }

    @Test
    public void testRedirectForChangesURL() throws Exception {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getChangesURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getChangesURL(run));
    }

    @Test
    public void testUrls() throws Exception {
        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/another", getRedirectedProvider().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/another", getRedirectedProvider().getJobURL(job));
        assertEquals(root + "job/my%20folder/job/my%20job/changesanother", getRedirectedProvider().getChangesURL(run));
    }

    @Test
    public void testGetTestUrl() throws Exception {
        TestResult result = mock(TestResult.class);

        AbstractTestResultAction action = mock(AbstractTestResultAction.class);
        when(action.getUrlName()).thenReturn("action");

        when(result.getRun()).thenReturn((Run)run);
        when(result.getTestResultAction()).thenReturn(action);
        when(result.getUrl()).thenReturn("/some id with spaces");

        String testUrl = getRedirectedProvider().getTestUrl(result);
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/job/my%20folder/job/my%20job/1/action/some%20id%20with%20spacesanother", testUrl);
    }

    @Override
    protected DisplayURLProvider getRedirectedProvider() {
        return Iterables.find(DisplayURLProvider.all(), Predicates.instanceOf(AnotherDisplayURLProvider.class));
    }

    @TestExtension
    public static class AnotherDisplayURLProvider extends DisplayURLProvider {

        public static final String EXTRA_CONTENT_IN_URL = "another";

        @Override
        public String getRunURL(Run<?, ?> run) {
            return DisplayURLProvider.getDefault().getRunURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @Override
        public String getChangesURL(Run<?, ?> run) {
            return DisplayURLProvider.getDefault().getChangesURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @Override
        public String getJobURL(Job<?, ?> project) {
            return DisplayURLProvider.getDefault().getJobURL(project) + EXTRA_CONTENT_IN_URL;
        }

        @Override
        public String getTestUrl(hudson.tasks.test.TestResult result) {
            return DisplayURLProvider.getDefault().getTestUrl(result) + EXTRA_CONTENT_IN_URL;
        }
    }
}
