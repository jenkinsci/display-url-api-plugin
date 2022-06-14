package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.displayurlapi.user.PreferredProviderUserProperty;
import org.junit.After;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;

public class ActionRedirectEligibilityTest extends AbstractActionRedirectTest {

    private static PreferredProviderUserProperty propToUse;

    @After
    public void tearDown() {
        System.setProperty("jenkins.displayurl.provider", "");
    }

    @Test
    public void testUserChooseImplementation() {
        // no user then different than Classic
        propToUse = null;
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getRunURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getEligibleRedirectedProvider().getRunURL(run));

        // user chooses a provider other than classic
        propToUse = new PreferredProviderUserProperty(EligibleDisplayURLProvider.class.getName());
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getRunURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getEligibleRedirectedProvider().getRunURL(run));


        // user chooses Classic provider
        propToUse = new PreferredProviderUserProperty(ClassicDisplayURLProvider.class.getName());
        given()
            .urlEncodingEnabled(false)
            .redirects().follow(false)
            .when().get(provider.getRunURL(run)).then()
            .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
            .header("Location", getRedirectedProvider().getRunURL(run));

        // user chooses default provider then different than Classic
        propToUse = new PreferredProviderUserProperty("default");
        given()
                .urlEncodingEnabled(false)
                .redirects().follow(false)
                .when().get(provider.getRunURL(run)).then()
                .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
                .header("Location", getEligibleRedirectedProvider().getRunURL(run));
    }

    @Test
    public void testUserDefaultImplementation() {
        assertThat(DisplayURLProvider.getDefault(), Matchers.instanceOf(ClassicDisplayURLProvider.class));

        System.setProperty("jenkins.displayurl.provider", EligibleDisplayURLProvider.class.getName());
        assertThat(DisplayURLProvider.getDefault(), Matchers.instanceOf(EligibleDisplayURLProvider.class));

        System.setProperty("jenkins.displayurl.provider", ClassicDisplayURLProvider.class.getName());
        assertThat(DisplayURLProvider.getDefault(), Matchers.instanceOf(ClassicDisplayURLProvider.class));
    }

    @Test
    public void testUserDefaultImplementationForRedirection() {
        //set Classic provider
        System.setProperty("jenkins.displayurl.provider", ClassicDisplayURLProvider.class.getName());
        given()
                .urlEncodingEnabled(false)
                .redirects().follow(false)
                .when().get(provider.getRunURL(run)).then()
                .statusCode(HttpServletResponse.SC_MOVED_TEMPORARILY)
                .header("Location", getRedirectedProvider().getRunURL(run));
    }

    @Override
    protected DisplayURLProvider getRedirectedProvider() {
        return DisplayURLProvider.all().stream()
            .filter(ClassicDisplayURLProvider.class::isInstance).findFirst()
            .orElse(null);
    }

    private DisplayURLProvider getEligibleRedirectedProvider() {
        return DisplayURLProvider.all().stream()
            .filter(EligibleDisplayURLProvider.class::isInstance).findFirst()
            .orElse(null);
    }

    @TestExtension
    public static class EligibleDisplayURLProvider extends DisplayURLProvider {

        public static final String ELIGIBLE_IN_URL = "http://eligible.com";

        @NonNull
        @Override
        public String getRunURL(Run<?, ?> run) {
            return ELIGIBLE_IN_URL;
        }

        @NonNull
        @Override
        public String getChangesURL(Run<?, ?> run) {
            return ELIGIBLE_IN_URL;
        }

        @Override
        public String getTestsURL(Run<?, ?> run) {
            return ELIGIBLE_IN_URL;
        }

        @NonNull
        @Override
        public String getArtifactsURL(Run<?, ?> run) {
            return ELIGIBLE_IN_URL;
        }

        @NonNull
        @Override
        public String getJobURL(Job<?, ?> project) {
            return ELIGIBLE_IN_URL;
        }
    }

    @TestExtension
    public static class TransientActionFactoryImpl extends TransientActionFactory {
        @Override
        public Class type() {
            return Run.class;
        }

        @NonNull
        @Override
        public Collection<? extends Action> createFor(@NonNull Object target) {
            return ImmutableList.of(new MockRunDisplayAction((Run) target));
        }
    }

    private static class MockRunDisplayAction extends RunDisplayAction {

        MockRunDisplayAction(Run run) {
            super(run);
        }

        @Override
        protected PreferredProviderUserProperty getUserPreferredProviderProperty() {
            return propToUse;
        }

    }

}
