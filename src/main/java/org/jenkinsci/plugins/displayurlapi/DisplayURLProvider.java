package org.jenkinsci.plugins.displayurlapi;

import com.google.common.collect.Iterables;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Project;
import hudson.model.Run;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestObject;
import jenkins.model.Jenkins;

/**
 * Generates URLs for well known UI locations for use in notifications (e.g. mailer, HipChat, Slack, IRC, etc)
 * Extensible to allow plugins to override common URLs (e.g. Blue Ocean or another future secondary UI)
 */
public abstract class DisplayURLProvider implements ExtensionPoint {

    private static final ClassicDisplayURLProvider CLASSIC_DISPLAY_URL_PROVIDER = new ClassicDisplayURLProvider();

    /**
     * Returns the first {@link DisplayURLProvider} found
     * @return DisplayURLProvider
     */
    public static DisplayURLProvider get() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins has not started");
        }
        return Iterables.getFirst(jenkins.getExtensionList(DisplayURLProvider.class), CLASSIC_DISPLAY_URL_PROVIDER);
    }

    /** Fully qualified URL for the Root display URL */
    public abstract String getRoot();

    /** Fully qualified URL for a Run */
    public abstract String getRunURL(Run<?, ?> run);

    /** Fully qualified URL for a page that displays changes for a project. */
    public abstract String getChangesURL(Run<?, ?> run);

    /** Fully qualified URL for a Jobs home */
    public abstract String getJobURL(Job<?, ?> project);

    /** Fully qualified URL to the test details page for a given test result */
    public abstract String getTestUrl(hudson.tasks.test.TestResult result);

    /** URL Factory for the Classical Jenkins UI */
    static class ClassicDisplayURLProvider extends DisplayURLProvider {
        @Override
        public String getRunURL(Run<?, ?> run) {
            return getRoot() + run.getUrl();
        }

        @Override
        public String getChangesURL(Run<?, ?> run) {
            return getJobURL(run.getParent()) + "changes";
        }

        @Override
        public String getJobURL(Job<?, ?> project) {
            return getRoot() + project.getUrl();
        }

        @Override
        public String getRoot() {
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) {
                throw new IllegalStateException("Jenkins has not started");
            }
            String root = jenkins.getRootUrl();
            if (root == null) {
                throw new IllegalStateException("Could not determine Jenkins URL. You should set one in Manage Jenkins.");
            }
            return root;
        }

        @Override
        public String getTestUrl(hudson.tasks.test.TestResult result) {
            String buildUrl = getRunURL(result.getRun());
            AbstractTestResultAction action = result.getTestResultAction();

            TestObject parent = result.getParent();
            TestResult testResultRoot = null;
            while(parent != null) {
                if (parent instanceof TestResult) {
                    testResultRoot = (TestResult) parent;
                    break;
                }
                parent = parent.getParent();
            }

            String testUrl = action.getUrlName()
                    + (testResultRoot != null ? testResultRoot.getUrl() : "")
                    + result.getUrl();

            String[] pathComponents = testUrl.split("/");
            StringBuilder buf = new StringBuilder();
            for (String c : pathComponents) {
                buf.append(Util.rawEncode(c)).append('/');
            }
            // remove last /
            buf.deleteCharAt(buf.length() - 1);

            return buildUrl + buf.toString();
        }
    }
}
