package org.jenkinsci.plugins.displayurlapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;

/**
 * Display URL Provider for the Classical Jenkins UI
 */
@Extension
public class ClassicDisplayURLProvider extends DisplayURLProvider {

    @Override
    @NonNull
    public String getDisplayName() {
        return Messages.classic_name();
    }

    @Override
    @NonNull
    public String getName() {
        return "classic";
    }

    @Override
    @NonNull
    public String getRunURL(Run<?, ?> run) {
        return getRoot() + Util.encode(run.getUrl());
    }

    @Override
    @NonNull
    public String getChangesURL(Run<?, ?> run) {
        return getJobURL(run.getParent()) + "changes";
    }

    @Override
    @NonNull
    public String getTestsURL(Run<?, ?> run) {
        return getRunURL(run) + "testReport";
    }

    @Override
    @NonNull
    public String getJobURL(Job<?, ?> job) {
        return getRoot() + Util.encode(job.getUrl());
    }
}
