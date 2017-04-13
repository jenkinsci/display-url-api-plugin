package org.jenkinsci.plugins.displayurlapi;

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
    public String getDisplayName() {
        return Messages.classic_name();
    }

    @Override
    public String getRunURL(Run<?, ?> run) {
        return getRoot() + Util.encode(run.getUrl());
    }

    @Override
    public String getChangesURL(Run<?, ?> run) {
        return getJobURL(run.getParent()) + "changes";
    }

    @Override
    public String getJobURL(Job<?, ?> job) {
        return getRoot() + Util.encode(job.getUrl());
    }
}
