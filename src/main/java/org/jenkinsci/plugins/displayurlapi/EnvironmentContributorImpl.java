package org.jenkinsci.plugins.displayurlapi;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;
import java.io.IOException;

@Extension
public class EnvironmentContributorImpl extends EnvironmentContributor {
    @Override
    public void buildEnvironmentFor(@Nonnull Run r, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
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
    public void buildEnvironmentFor(@Nonnull Job j, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
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
