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
        DisplayURLContext ctx = DisplayURLContext.open();
        try {
            ctx.run(r);
            ctx.plugin(null); // environment contributor "comes from" core
            DisplayURLProvider urlProvider = DisplayURLProvider.get();
            envs.put("RUN_DISPLAY_URL", urlProvider.getRunURL(r));
            envs.put("RUN_CHANGES_DISPLAY_URL", urlProvider.getChangesURL(r));
        } finally {
            ctx.close();
        }
    }

    @Override
    public void buildEnvironmentFor(@NonNull Job j, @NonNull EnvVars envs, @NonNull TaskListener listener) throws IOException, InterruptedException {
        DisplayURLContext ctx = DisplayURLContext.open();
        try {
            ctx.job(j);
            ctx.plugin(null); // environment contributor "comes from" core
            envs.put("JOB_DISPLAY_URL", DisplayURLProvider.get().getJobURL(j));
        } finally {
            ctx.close();
        }
    }
}
