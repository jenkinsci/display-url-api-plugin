package org.jenkinsci.plugins.displayurlapi;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.actions.AbstractDisplayAction;


import javax.annotation.Nullable;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

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
        return Jenkins.getActiveInstance().getExtensionList(DisplayURLProvider.class);
    }

    public static DisplayURLProvider getDefault() {
        DisplayURLProvider defaultProvider = getPreferredProvider();
        if (defaultProvider == null) {
            defaultProvider = ExtensionList.lookup(DisplayURLProvider.class).get(ClassicDisplayURLProvider.class);
        }
        return defaultProvider;
    }

    /** Fully qualified URL for the Root display URL */
    public String getRoot() {
        String root = Jenkins.getActiveInstance().getRootUrl();
        if (root == null) {
            root = "http://unconfigured-jenkins-location/";
        }
        return Util.encode(root);
    }

    /** Display name of this provider e.g. "Jenkins Classic", "Blue Ocean", etc */
    public String getDisplayName() {
        return this.getClass().getSimpleName();
    }

    /** Name of provider to be used as an id. Do not use i18n */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /** Fully qualified URL for a Run */
    public abstract String getRunURL(Run<?, ?> run);

    /** Fully qualified URL for a page that displays changes for a project. */
    public abstract String getChangesURL(Run<?, ?> run);

    /** Fully qualified URL for a Jobs home */
    public abstract String getJobURL(Job<?, ?> job);

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
    }

    private static String findClass() {
        String clazz = System.getenv(JENKINS_DISPLAYURL_PROVIDER_ENV);
        if (StringUtils.isEmpty(clazz)) {
            clazz = System.getProperty(JENKINS_DISPLAYURL_PROVIDER_PROP);
        }
        return clazz;
    }

    @Nullable
    public static DisplayURLProvider getPreferredProvider() {
        String clazz = findClass();
        if (isNotEmpty(clazz)) {
            return ExtensionList.lookup(DisplayURLProvider.class).getDynamic(clazz);
        }
        return null;
    }


    private static final String JENKINS_DISPLAYURL_PROVIDER_ENV = "JENKINS_DISPLAYURL_PROVIDER";
    private static final String JENKINS_DISPLAYURL_PROVIDER_PROP = "jenkins.displayurl.provider";
}
