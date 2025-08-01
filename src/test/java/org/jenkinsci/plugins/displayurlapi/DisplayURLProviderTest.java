package org.jenkinsci.plugins.displayurlapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.PluginWrapper;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.displayurlapi.actions.RunDisplayAction;
import org.jenkinsci.plugins.displayurlapi.user.PreferredProviderUserProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithJenkins
class DisplayURLProviderTest {
    private JenkinsRule rule;
    private String flag;

    @BeforeEach
    void setUp(JenkinsRule r) {
        rule = r;
        flag = System.clearProperty(DisplayURLProvider.JENKINS_DISPLAYURL_PROVIDER_PROP);
    }

    @AfterEach
    void tearDown() {
        if (flag != null) {
            System.setProperty(DisplayURLProvider.JENKINS_DISPLAYURL_PROVIDER_PROP, flag);
        }
    }

    @Test
    void urls() throws Exception {
        DefaultDisplayURLProviderGlobalConfiguration.get().setProviderId(
                ClassicDisplayURLProvider.class.getName()
        );

        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject p = folder.createProject(FreeStyleProject.class, "my job");
        Run<?, ?> b = rule.buildAndAssertSuccess(p);
        assertExternalUrls(p, b);
        assertEquals(DisplayURLProvider.get().getRoot() + "job/my%20folder/job/my%20job/1/",
                     b.getAction(RunDisplayAction.class).getDisplayUrl());
    }

    @TestExtension("urlsWithSysPropProvider")
    public static class TestSysPropDisplayURLProvider extends DisplayURLProvider
    {

        public static final String EXTRA_CONTENT_IN_URL = "syspropsdefinedprovider";

        private DisplayURLProvider getRootProvider()
        {
            return ExtensionList.lookup( DisplayURLProvider.class ).getDynamic(
                ClassicDisplayURLProvider.class.getName() );
        }

        @NonNull
        @Override
        public String getRunURL( Run<?, ?> run )
        {
            return getRootProvider().getRunURL( run ) + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getArtifactsURL( Run<?, ?> run )
        {
            return getRootProvider().getArtifactsURL( run ) + "/" + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getChangesURL( Run<?, ?> run )
        {
            return getRootProvider().getChangesURL( run ) + "/" + EXTRA_CONTENT_IN_URL;
        }

        @Override
        public String getTestsURL( Run<?, ?> run )
        {
            return getRootProvider().getTestsURL( run ) + "/" + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getJobURL( Job<?, ?> project )
        {
            return getRootProvider().getJobURL( project ) + EXTRA_CONTENT_IN_URL;
        }
    }

    @Test
    void urlsWithSysPropProvider() throws Exception {
        System.setProperty(DisplayURLProvider.JENKINS_DISPLAYURL_PROVIDER_PROP,
                           TestSysPropDisplayURLProvider.class.getName());
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject p = folder.createProject(FreeStyleProject.class, "my job");
        Run<?, ?> b = rule.buildAndAssertSuccess(p);
        assertExternalUrls(p, b);
        assertEquals(DisplayURLProvider.get().getRoot() + "job/my%20folder/job/my%20job/1/" + TestSysPropDisplayURLProvider.EXTRA_CONTENT_IN_URL,
                b.getAction(RunDisplayAction.class).getDisplayUrl());
    }

    @TestExtension(value = { "urlsWithUserDefinedProvider", "providerConfigurationPrecedence" })
    public static class TestUserDisplayURLProvider extends DisplayURLProvider
    {

        public static final String EXTRA_CONTENT_IN_URL = "userdefinedprovider";

        private DisplayURLProvider getRootProvider()
        {
            return ExtensionList.lookup( DisplayURLProvider.class ).getDynamic(
                ClassicDisplayURLProvider.class.getName() );
        }

        @NonNull
        @Override
        public String getRunURL( Run<?, ?> run )
        {
            return getRootProvider().getRunURL( run ) + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getArtifactsURL( Run<?, ?> run )
        {
            return getRootProvider().getArtifactsURL( run ) + "/" + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getChangesURL( Run<?, ?> run )
        {
            return getRootProvider().getChangesURL( run ) + "/" + EXTRA_CONTENT_IN_URL;
        }

        @Override
        public String getTestsURL( Run<?, ?> run )
        {
            return getRootProvider().getTestsURL( run ) + "/" + EXTRA_CONTENT_IN_URL;
        }

        @NonNull
        @Override
        public String getJobURL( Job<?, ?> project )
        {
            return getRootProvider().getJobURL( project ) + EXTRA_CONTENT_IN_URL;
        }
    }

    @Test
    void urlsWithUserDefinedProvider() throws Exception {
        rule.jenkins.setSecurityRealm(rule.createDummySecurityRealm());
        User foo = User.getById("foo", true);

        PreferredProviderUserProperty userProperty =
            new PreferredProviderUserProperty(TestUserDisplayURLProvider.class.getName());
        foo.addProperty(userProperty);
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject p = folder.createProject(FreeStyleProject.class, "my job");
        Run<?, ?> b = rule.buildAndAssertSuccess(p);
        try (ACLContext unused = ACL.as(foo)) {
            assertExternalUrls(p, b);
            assertEquals(DisplayURLProvider.get().getRoot() + "job/my%20folder/job/my%20job/1/" + TestUserDisplayURLProvider.EXTRA_CONTENT_IN_URL,
                    b.getAction(RunDisplayAction.class).getDisplayUrl());
        }
    }

    @Test
    void decoration() throws Exception {
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject project = (FreeStyleProject) folder
                .createProject(rule.jenkins.getDescriptorByType(FreeStyleProject.DescriptorImpl.class), "my job",
                        false);
        Run<?, ?> run = project.scheduleBuild2(0).get();

        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getURL().getPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?utm_campaign=jenkins&utm_source=Jenkins"
                + "&utm_term=my+folder%2Fmy+job%231", DisplayURLProvider.get().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/redirect?utm_campaign=jenkins&utm_source=Jenkins",
                DisplayURLProvider.get().getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=artifacts&utm_campaign=jenkins"
                        + "&utm_source=Jenkins&utm_term=my+folder%2Fmy+job%231",
                DisplayURLProvider.get().getArtifactsURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=changes&utm_campaign=jenkins"
                        + "&utm_source=Jenkins&utm_term=my+folder%2Fmy+job%231",
                DisplayURLProvider.get().getChangesURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=tests&utm_campaign=jenkins"
                        + "&utm_source=Jenkins&utm_term=my+folder%2Fmy+job%231",
                DisplayURLProvider.get().getTestsURL(run));
        try (DisplayURLContext ctx = DisplayURLContext.open()) {
            ctx.plugin(Jenkins.get().getPluginManager().getPlugin("display-url-api"));
            assertEquals(
                    root
                            + "job/my%20folder/job/my%20job/1/display/redirect?utm_campaign=display-url-api"
                            + "&utm_source=Jenkins&utm_term=my+folder%2Fmy+job%231", DisplayURLProvider.get().getRunURL(run));
            assertEquals(root
                            + "job/my%20folder/job/my%20job/display/redirect?utm_campaign=display-url-api&utm_source"
                            + "=Jenkins",
                    DisplayURLProvider.get().getJobURL(project));
            assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=artifacts&utm_campaign=display"
                            + "-url-api&utm_source=Jenkins&utm_term=my+folder%2Fmy+job%231",
                    DisplayURLProvider.get().getArtifactsURL(run));
            assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=changes&utm_campaign=display"
                            + "-url-api&utm_source=Jenkins&utm_term=my+folder%2Fmy+job%231",
                    DisplayURLProvider.get().getChangesURL(run));
            assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=tests&utm_campaign=display"
                            + "-url-api&utm_source=Jenkins&utm_term=my+folder%2Fmy+job%231",
                    DisplayURLProvider.get().getTestsURL(run));
        }

        EnvVars environment = run.getEnvironment();
        assertEquals(DisplayURLProvider.get().getRunURL(run), environment.get("RUN_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getArtifactsURL(run), environment.get("RUN_ARTIFACTS_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getChangesURL(run), environment.get("RUN_CHANGES_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getTestsURL(run), environment.get("RUN_TESTS_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getJobURL(project), environment.get("JOB_DISPLAY_URL"));
    }

    @Test
    void providerConfigurationPrecedence() throws Exception {
        rule.jenkins.setSecurityRealm(rule.createDummySecurityRealm());
        // user1 does not have a preference, but user2 does.
        User user1 = User.getById("user1", true);
        User user2 = User.getById("user2", true);
        user2.addProperty(new PreferredProviderUserProperty(ClassicDisplayURLProvider.class.getName()));
        // admin configures TestUserDisplayURLProvider as the default provider.
        DefaultDisplayURLProviderGlobalConfiguration.get().setProviderId(TestUserDisplayURLProvider.class.getName());
        FreeStyleProject p = rule.createFreeStyleProject();
        Run<?, ?> b = rule.buildAndAssertSuccess(p);
        try (ACLContext unused = ACL.as(user1)) {
            assertEquals(rule.getURL() + b.getUrl() + TestUserDisplayURLProvider.EXTRA_CONTENT_IN_URL, DisplayURLProvider.getPreferredProvider().getRunURL(b));
        }
        try (ACLContext unused = ACL.as(user2)) {
            assertEquals(rule.getURL() + b.getUrl(), DisplayURLProvider.getPreferredProvider().getRunURL(b));
        }
    }

    private void assertExternalUrls(Job<?, ?> project, Run<?, ?> run) throws Exception {
        // No matter what configuration is being used, this plugin should always produce .../display/redirect URLs.
        // Configurations should only be applied when resolving a redirect URL.
        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getURL().getPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect", DisplayURLProvider.get().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/redirect",
                DisplayURLProvider.get().getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=artifacts",
                DisplayURLProvider.get().getArtifactsURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=changes",
                DisplayURLProvider.get().getChangesURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=tests",
                DisplayURLProvider.get().getTestsURL(run));

        EnvVars environment = run.getEnvironment();
        assertEquals(DisplayURLProvider.get().getRunURL(run), environment.get("RUN_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getArtifactsURL(run), environment.get("RUN_ARTIFACTS_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getChangesURL(run), environment.get("RUN_CHANGES_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getTestsURL(run), environment.get("RUN_TESTS_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getJobURL(project), environment.get("JOB_DISPLAY_URL"));
    }

    @TestExtension("decoration")
    public static class DisplayURLDecoratorImpl extends DisplayURLDecorator {
        @NonNull
        @Override
        protected Map<String, String> parameters(@NonNull DisplayURLContext context) {
            Map<String, String> result = new HashMap<>();
            PluginWrapper wrapper = context.plugin();
            result.put("utm_source", "Jenkins");
            result.put("utm_campaign", wrapper == null ? "jenkins" : wrapper.getShortName());
            String medium = context.attribute("medium");
            if (medium != null) {
                result.put("utm_medium", medium);
            }
            Run run = context.run();
            if (run != null) {
                result.put("utm_term", run.getExternalizableId());
            }
            return result;
        }
    }
}
