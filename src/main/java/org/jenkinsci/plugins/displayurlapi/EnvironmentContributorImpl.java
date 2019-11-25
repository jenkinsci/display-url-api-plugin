package org.jenkinsci.plugins.displayurlapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;

@Extension
public class EnvironmentContributorImpl extends EnvironmentContributor {

    @Override
    public void buildEnvironmentFor(@NonNull Run r, @NonNull EnvVars envs, @NonNull TaskListener listener) throws IOException, InterruptedException {
        try (DisplayURLContext ctx = DisplayURLContext.open(false)) { // environment contributor "comes from" core
            ctx.run(r);
            DisplayURLProvider urlProvider = DisplayURLProvider.get();
            envs.put("RUN_DISPLAY_URL", urlProvider.getRunURL(r));
            envs.put("RUN_ARTIFACTS_DISPLAY_URL", urlProvider.getArtifactsURL(r));
            envs.put("RUN_CHANGES_DISPLAY_URL", urlProvider.getChangesURL(r));
            envs.put("RUN_TESTS_DISPLAY_URL", urlProvider.getTestsURL(r));
        }
    }

    @Override
    public void buildEnvironmentFor(@NonNull Job j, @NonNull EnvVars envs, @NonNull TaskListener listener) throws IOException, InterruptedException {
        try (DisplayURLContext ctx = DisplayURLContext.open(false)) {
            ctx.job(j);
            envs.put("JOB_DISPLAY_URL", DisplayURLProvider.get().getJobURL(j));
        }
    }
}
