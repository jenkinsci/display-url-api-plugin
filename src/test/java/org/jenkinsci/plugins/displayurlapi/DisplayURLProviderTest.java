package org.jenkinsci.plugins.displayurlapi;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DisplayURLProviderTest {

    @Rule
    public JenkinsRuleWithLocalPort rule = new JenkinsRuleWithLocalPort();
    @Mock
    private Jenkins jenkins;
    @Mock
    private Run<?, ?> run;
    @Mock
    private Job<?, ?> job;

    @Test
    public void urls() {
        String prefix = "http://localhost:" + rule.getLocalPort() + "/jenkins";
        assertEquals(prefix + "job/foo/32/", DisplayURLProvider.get().getRunURL(run));
        assertEquals(prefix + "job/foo/", DisplayURLProvider.get().getJobURL(job));
        assertEquals(prefix + "job/foo/changes", DisplayURLProvider.get().getChangesURL(run));
    }

    @Before
    public void setupMocks() throws Exception {
        given(job.getUrl()).willReturn("job/foo/");
        given(run.getUrl()).willReturn("job/foo/32/");
        given(run.getParent()).willReturn((Job)job);
    }

    class JenkinsRuleWithLocalPort extends JenkinsRule {
        public int getLocalPort() {
            return this.localPort;
        }
    }
}
