package org.jenkinsci.plugins.displayurlapi.actions;

import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.displayurlapi.DefaultDisplayURLProviderGlobalConfiguration;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.servlet.http.HttpServletResponse;

import java.net.HttpURLConnection;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class ActionRedirectClassicTest extends AbstractActionRedirectTest {

    @Before
    public void setup() {
        DefaultDisplayURLProviderGlobalConfiguration.get().setProviderId(
                ClassicDisplayURLProvider.class.getName()
        );
    }

    @Test
    public void testRedirectForJobURL() throws Exception {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getJobURL(job)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getJobURL(job));

        JenkinsRule.WebClient wc = rule.createWebClient()
                .withRedirectEnabled(true)
                .withThrowExceptionOnFailingStatusCode(false);

        WebResponse rsp = wc.getPage(new WebRequest(new URL(provider.getJobURL(job)))).getWebResponse();
        assertEquals(rsp.getContentAsString(), HttpURLConnection.HTTP_OK, rsp.getStatusCode());
    }

    @Test
    public void testRedirectForRunURL() throws Exception {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getRunURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getRunURL(run));

        JenkinsRule.WebClient wc = rule.createWebClient()
                .withRedirectEnabled(true)
                .withThrowExceptionOnFailingStatusCode(false);

        WebResponse rsp = wc.getPage(new WebRequest(new URL(provider.getRunURL(run)))).getWebResponse();
        assertEquals(rsp.getContentAsString(), HttpURLConnection.HTTP_OK, rsp.getStatusCode());
    }

    @Test
    public void testRedirectForArtifactsURL() throws Exception {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getArtifactsURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getArtifactsURL(run));

        JenkinsRule.WebClient wc = rule.createWebClient()
                .withRedirectEnabled(true)
                .withThrowExceptionOnFailingStatusCode(false);

        WebResponse rsp = wc.getPage(new WebRequest(new URL(provider.getArtifactsURL(run)))).getWebResponse();
        assertEquals(rsp.getContentAsString(), HttpURLConnection.HTTP_OK, rsp.getStatusCode());
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

    @Override
    protected DisplayURLProvider getRedirectedProvider() {
        return Iterables.find(DisplayURLProvider.all(), Predicates.instanceOf(ClassicDisplayURLProvider.class));
    }
}
