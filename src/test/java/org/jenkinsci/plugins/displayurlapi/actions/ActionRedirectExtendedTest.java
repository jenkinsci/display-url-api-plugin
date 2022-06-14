package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import javax.servlet.http.HttpServletResponse;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class ActionRedirectExtendedTest extends AbstractActionRedirectTest {

    @Test
    public void testRedirectForJobURL() {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getJobURL(job)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getJobURL(job));
    }

    @Test
    public void testRedirectForRunURL() {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getRunURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getRunURL(run));
    }

    @Test
    public void testRedirectForArtifactsURL() {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getArtifactsURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getArtifactsURL(run));
    }

    @Test
    public void testRedirectForChangesURL() {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getChangesURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getChangesURL(run));
    }

    @Test
    public void testRedirectForTestsURL() {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getTestsURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getTestsURL(run));
    }

    @Test
    public void testRedirectForYetAnotherProviderParameter() {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getChangesURL(run) + "&provider=YetAnotherDisplayURLProvider").then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getYetAnotherRedirectedProvider().getChangesURL(run));
    }

    @Test
    public void testUrls() {
        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/another", getRedirectedProvider().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/another", getRedirectedProvider().getJobURL(job));
        assertEquals(root + "job/my%20folder/job/my%20job/1/artifactanother", getRedirectedProvider().getArtifactsURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/changesanother", getRedirectedProvider().getChangesURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/1/testReportanother", getRedirectedProvider().getTestsURL(run));
    }


    @Override
    protected DisplayURLProvider getRedirectedProvider() {
        return Iterables.find(DisplayURLProvider.all(), Predicates.instanceOf(AnotherDisplayURLProvider.class));
    }

    protected DisplayURLProvider getYetAnotherRedirectedProvider() {
        return Iterables.find(DisplayURLProvider.all(), Predicates.instanceOf(YetAnotherDisplayURLProvider.class));
    }

    @TestExtension
    public static class AnotherDisplayURLProvider extends DisplayURLProvider {

        public static final String EXTRA_CONTENT_IN_URL = "another";

        @NonNull
        @Override
        public String getRunURL(Run<?, ?> run) {
            return DisplayURLProvider.getDefault().getRunURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getArtifactsURL(Run<?, ?> run) {
            return DisplayURLProvider.getDefault().getArtifactsURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getChangesURL(Run<?, ?> run) {
            return DisplayURLProvider.getDefault().getChangesURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @Override
        public String getTestsURL(Run<?, ?> run) {
            return DisplayURLProvider.getDefault().getTestsURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getJobURL(Job<?, ?> project) {
            return DisplayURLProvider.getDefault().getJobURL(project) + EXTRA_CONTENT_IN_URL;
        }
    }

    @TestExtension
    public static class YetAnotherDisplayURLProvider extends DisplayURLProvider {

        public static final String EXTRA_CONTENT_IN_URL = "yetanother";

        @NonNull
        @Override
        public String getRunURL(Run<?, ?> run) {
            return DisplayURLProvider.getDefault().getRunURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getArtifactsURL(Run<?, ?> run) {
            return DisplayURLProvider.getDefault().getArtifactsURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getChangesURL(Run<?, ?> run) {
            return DisplayURLProvider.getDefault().getChangesURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @Override
        public String getTestsURL(Run<?, ?> run) {
            return DisplayURLProvider.getDefault().getTestsURL(run) + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getJobURL(Job<?, ?> project) {
            return DisplayURLProvider.getDefault().getJobURL(project) + EXTRA_CONTENT_IN_URL;
        }
    }
}
