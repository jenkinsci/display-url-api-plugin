package org.jenkinsci.plugins.displayurlapi;

import com.google.common.collect.Iterables;
import hudson.ExtensionPoint;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Project;
import hudson.model.Run;
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
        return Iterables.getFirst(Jenkins.getInstance().getExtensionList(DisplayURLProvider.class), CLASSIC_DISPLAY_URL_PROVIDER);
    }

    /** Fully qualified URL for a Run */
    public abstract String getRunURL(Run<?, ?> run);

    /** Fully qualified URL for a page that displays changes for a project. */
    public abstract String getChangesURL(Run<?, ?> run);

    /** Fully qualified URL for a Jobs home */
    public abstract String getJobURL(Job<?, ?> project);

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

        static String getRoot() {
            String root = Jenkins.getInstance().getRootUrl();
            if (root == null) {
                throw new IllegalStateException("Could not determine Jenkins URL. You should set one in Manage Jenkins.");
            }
            if (root.endsWith("/")) {
                root = root.substring(0, root.length() - 1);
            }
            return root;
        }
    }
}
