package org.jenkinsci.plugins.displayurlapi;

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
    public String getDisplayName() {
        return Messages.classic_name();
    }

    @Override
    public String getName() {
        return "classic";
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
    public String getTestsURL(Run<?, ?> run) {
        return getRunURL(run) + this.getTestsFolder();
    }

    @Override
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
