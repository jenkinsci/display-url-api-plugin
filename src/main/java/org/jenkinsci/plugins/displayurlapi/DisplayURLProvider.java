package org.jenkinsci.plugins.displayurlapi;

import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.displayurlapi.actions.AbstractDisplayAction;

/**
 * Generates URLs for well known UI locations for use in notifications (e.g. mailer, HipChat, Slack, IRC, etc)
 * Extensible to allow plugins to override common URLs (e.g. Blue Ocean or another future secondary UI)
 */
public abstract class DisplayURLProvider implements ExtensionPoint {

    /**
     * @return DisplayURLProvider
     */
    public static DisplayURLProvider get() {
        return DisplayURLProviderImpl.INSTANCE;
    }

    public static Iterable<DisplayURLProvider> all() {
        return getJenkins().getExtensionList(DisplayURLProvider.class);
    }

    /** Fully qualified URL for the Root display URL */
    public String getRoot() {
        String root = getJenkins().getRootUrl();
        if (root == null) {
            root = "http://unconfigured-jenkins-location/";
        }
        return Util.encode(root);
    }

    /** Fully qualified URL for a Run */
    public abstract String getRunURL(Run<?, ?> run);

    /** Fully qualified URL for a page that displays changes for a project. */
    public abstract String getChangesURL(Run<?, ?> run);

    /** Fully qualified URL for a Jobs home */
    public abstract String getJobURL(Job<?, ?> job);

    /** Fully qualified URL to the test details page for a given test result */
    public abstract String getTestUrl(hudson.tasks.test.TestResult result);

    static class DisplayURLProviderImpl extends ClassicDisplayURLProvider {

        public static final DisplayURLProvider INSTANCE = new DisplayURLProviderImpl();

        public static final String DISPLAY_POSTFIX = AbstractDisplayAction.URL_NAME + "/redirect";

        @Override
        public String getRunURL(Run<?, ?> run) {
            return super.getRunURL(run) + DISPLAY_POSTFIX;
        }

        @Override
        public String getChangesURL(Run<?, ?> run) {
            return super.getRunURL(run) + DISPLAY_POSTFIX + "?page=changes";
        }

        @Override
        public String getJobURL(Job<?, ?> job) {
            return super.getJobURL(job) + DISPLAY_POSTFIX;
        }

        @Override
        public String getTestUrl(hudson.tasks.test.TestResult result) {
            Run<?, ?> run = result.getRun();
            return super.getRunURL(run) + DISPLAY_POSTFIX + "?page=test&id=" + Util.rawEncode(result.getId());
        }
    }

    private static Jenkins getJenkins() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins has not started");
        }
        return jenkins;
    }
}
