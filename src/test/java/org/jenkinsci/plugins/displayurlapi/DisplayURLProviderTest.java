package org.jenkinsci.plugins.displayurlapi;

import hudson.EnvVars;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.assertEquals;

public class DisplayURLProviderTest {

    @Rule
    public JenkinsRuleWithLocalPort rule = new JenkinsRuleWithLocalPort();

    @Test
    public void urls() throws Exception {
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject project = (FreeStyleProject) folder.createProject(rule.jenkins.getDescriptorByType(FreeStyleProject.DescriptorImpl.class), "my job", false);
        Run<?, ?> run = project.scheduleBuild2(0).get();

        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/", root);
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect", DisplayURLProvider.get().getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/redirect", DisplayURLProvider.get().getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=changes", DisplayURLProvider.get().getChangesURL(run));

        EnvVars environment = run.getEnvironment();
        assertEquals(DisplayURLProvider.get().getRunURL(run)+"?utm_source=jenkins&utm_medium=build-environment", environment.get("RUN_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getChangesURL(run) + "&utm_source=jenkins&utm_medium=build-environment", environment.get("RUN_CHANGES_DISPLAY_URL"));
        assertEquals(DisplayURLProvider.get().getJobURL(project) + "?utm_source=jenkins&utm_medium=build-environment", environment.get("JOB_DISPLAY_URL"));
    }

    @Test
    public void utm() throws Exception {
        MockFolder folder = rule.createFolder("my folder");
        FreeStyleProject project = (FreeStyleProject) folder
                .createProject(rule.jenkins.getDescriptorByType(FreeStyleProject.DescriptorImpl.class), "my job",
                        false);
        Run<?, ?> run = project.scheduleBuild2(0).get();

        String root = DisplayURLProvider.get().getRoot();
        assertEquals("http://localhost:" + rule.getLocalPort() + "/jenkins/", root);

        UTMDisplayURLProvider instance = DisplayURLProvider.get("unit_test");
        assertEquals(root+"?utm_source=jenkins&utm_medium=unit_test", instance.getRoot());
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?utm_source=jenkins&utm_medium=unit_test", instance.getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/redirect?utm_source=jenkins&utm_medium=unit_test", instance.getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=changes&utm_source=jenkins"
                + "&utm_medium=unit_test", instance.getChangesURL(run));

        instance = instance.withCampaign("testing% now");
        assertEquals(root+"?utm_source=jenkins&utm_medium=unit_test&utm_campaign=testing%25+now", instance.getRoot());
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?utm_source=jenkins&utm_medium=unit_test&utm_campaign=testing%25+now", instance.getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/redirect?utm_source=jenkins&utm_medium=unit_test&utm_campaign=testing%25+now", instance.getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=changes&utm_source=jenkins"
                + "&utm_medium=unit_test&utm_campaign=testing%25+now", instance.getChangesURL(run));

        instance = instance.withTerm("unit");
        assertEquals(root+"?utm_source=jenkins&utm_medium=unit_test&utm_campaign=testing%25+now&utm_term=unit", instance.getRoot());
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?utm_source=jenkins&utm_medium=unit_test&utm_campaign=testing%25+now&utm_term=unit", instance.getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/redirect?utm_source=jenkins&utm_medium=unit_test&utm_campaign=testing%25+now&utm_term=unit", instance.getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=changes&utm_source=jenkins"
                + "&utm_medium=unit_test&utm_campaign=testing%25+now&utm_term=unit", instance.getChangesURL(run));

        instance = instance.withContent("data");
        assertEquals(root+"?utm_source=jenkins&utm_medium=unit_test&utm_campaign=testing%25+now&utm_term=unit&utm_content=data", instance.getRoot());
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?utm_source=jenkins&utm_medium=unit_test&utm_campaign=testing%25+now&utm_term=unit&utm_content=data", instance.getRunURL(run));
        assertEquals(root + "job/my%20folder/job/my%20job/display/redirect?utm_source=jenkins&utm_medium=unit_test&utm_campaign=testing%25+now&utm_term=unit&utm_content=data", instance.getJobURL(project));
        assertEquals(root + "job/my%20folder/job/my%20job/1/display/redirect?page=changes&utm_source=jenkins"
                + "&utm_medium=unit_test&utm_campaign=testing%25+now&utm_term=unit&utm_content=data", instance.getChangesURL(run));
    }
}
