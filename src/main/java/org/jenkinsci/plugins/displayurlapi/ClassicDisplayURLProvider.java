package org.jenkinsci.plugins.displayurlapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import org.apache.commons.lang.StringUtils;

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
        return getRunURL(run) + this.getTestsFolder();
    }

    @Override
    @NonNull
    public String getJobURL(Job<?, ?> job) {
        return getRoot() + Util.encode(job.getUrl());
    }

    private String getTestsFolder() {
        String folder = System.getProperty(JENKINS_CLASSIC_DISPLAYURL_TESTS_FOLDER_PROP);
        if (StringUtils.isEmpty(folder)) {
            folder = "testReport";
        }
        return folder;
    }

    private static final String JENKINS_CLASSIC_DISPLAYURL_TESTS_FOLDER_PROP = "jenkins.classic.displayurl.tests.folder";
}
