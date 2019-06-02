package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import static io.restassured.RestAssured.given;

public class ActionRedirectClassicTest extends AbstractActionRedirectTest {

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
    public void testRedirectForArtifactsURL() throws Exception {
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getArtifactsURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getArtifactsURL(run));
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
    public void testRedirectForTestsURL() throws Exception {
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
