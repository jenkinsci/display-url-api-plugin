package org.jenkinsci.plugins.displayurlapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.PluginWrapper;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import java.util.HashMap;
import java.util.Map;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;

import static org.junit.Assert.assertEquals;

public class DisplayURLProviderTest {

    @Rule
    public JenkinsRuleWithLocalPort rule = new JenkinsRuleWithLocalPort();

    @Test
    public void urls() throws Exception {
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject project = (FreeStyleProject) folder
                .createProject(rule.jenkins.getDescriptorByType(FreeStyleProject.DescriptorImpl.class), "my job",
                        false);
        Run<?, ?> run = project.scheduleBuild2(0).get();

        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/", DisplayURLProvider.get().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/",
                DisplayURLProvider.get().getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/?page=changes",
                DisplayURLProvider.get().getChangesURL(run));

        EnvVars environment = run.getEnvironment();
        assertEquals(DisplayURLProvider.get().getRunURL(run), environment.get("RUN_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getChangesURL(run), environment.get("RUN_CHANGES_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getJobURL(project), environment.get("JOB_DISPLAY_URL"));
    }

    @Test
    public void decoration() throws Exception {
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject project = (FreeStyleProject) folder
                .createProject(rule.jenkins.getDescriptorByType(FreeStyleProject.DescriptorImpl.class), "my job",
                        false);
        Run<?, ?> run = project.scheduleBuild2(0).get();

        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/?utm_campaign=jenkins&utm_source=Jenkins"
                + "&utm_term=my+folder%2Fmy+job%231", DisplayURLProvider.get().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/?utm_campaign=jenkins&utm_source=Jenkins",
                DisplayURLProvider.get().getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/?page=changes&utm_campaign=jenkins"
                        + "&utm_source=Jenkins&utm_term=my+folder%2Fmy+job%231",
                DisplayURLProvider.get().getChangesURL(run));
        DisplayURLContext ctx = DisplayURLContext.open();
        try {
            ctx.plugin(Jenkins.getActiveInstance().getPluginManager().getPlugin("display-url-api"));
            assertEquals(
                    root
                            + "job/my%20folder/job/my%20job/1/display/?utm_campaign=display-url-api"
                            + "&utm_source=Jenkins&utm_term=my+folder%2Fmy+job%231", DisplayURLProvider.get().getRunURL(run));
            assertEquals(root
                            + "job/my%20folder/job/my%20job/display/?utm_campaign=display-url-api&utm_source"
                            + "=Jenkins",
                    DisplayURLProvider.get().getJobURL(project));
            assertEquals(root + "job/my%20folder/job/my%20job/1/display/?page=changes&utm_campaign=display"
                            + "-url-api&utm_source=Jenkins&utm_term=my+folder%2Fmy+job%231",
                    DisplayURLProvider.get().getChangesURL(run));
        } finally {
            ctx.close();
        }

        EnvVars environment = run.getEnvironment();
        assertEquals(DisplayURLProvider.get().getRunURL(run), environment.get("RUN_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getChangesURL(run), environment.get("RUN_CHANGES_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getJobURL(project), environment.get("JOB_DISPLAY_URL"));

    }

    @TestExtension("decoration")
    public static class DisplayURLDecoratorImpl extends DisplayURLDecorator {
        @NonNull
        @Override
        protected Map<String, String> parameters(@NonNull DisplayURLContext context) {
            Map<String, String> result = new HashMap<String, String>();
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
