package org.jenkinsci.plugins.displayurlapi;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.actions.AbstractDisplayAction;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Generates URLs for well known UI locations for use in notifications (e.g. mailer, HipChat, Slack, IRC, etc)
 * Extensible to allow plugins to override common URLs (e.g. Blue Ocean or another future secondary UI)
 */
public abstract class DisplayURLProvider implements ExtensionPoint {

    /**
     * @return DisplayURLProvider
     * @deprecated use {@link #get(String)}
     */
    public static DisplayURLProvider get() {
        return DisplayURLProviderImpl.INSTANCE;
    }

    /**
     * Returns a {@link DisplayURLProvider} that will apply the {@code utm_source=jenkins} and {@code utm_medium=}
     * for the supplied parameter to all generated URLs.
     * @param medium the {@code utm_medium}.
     * @return the {@link DisplayURLProvider}.
     * @see UTMDisplayURLProvider#withCampaign(String) to set the {@code utm_campaign}
     * @see UTMDisplayURLProvider#withTerm(String) to set the {@code utm_term}
     * @see UTMDisplayURLProvider#withContent(String) to set the {@code utm_content}
     * @see UTMDisplayURLProvider#withSource(String) to set the {@code utm_source}
     * @since 2.0
     */
    public static UTMDisplayURLProvider get(String medium) {
        return new UTMDisplayURLProvider(get(), medium);
    }

    public static Iterable<DisplayURLProvider> all() {
        return getJenkins().getExtensionList(DisplayURLProvider.class);
    }

    public static DisplayURLProvider getDefault() {
        DisplayURLProvider defaultProvider = Iterables.find(all(), Predicates.instanceOf(ClassicDisplayURLProvider.class));

        // Get the default provider from environment variable or system property
        final String clazz = findClass();
        if (isNotEmpty(clazz)) {
            defaultProvider = Iterables.find(DisplayURLProvider.all(), new Predicate<DisplayURLProvider>() {
                @Override
                public boolean apply(DisplayURLProvider input) {
                    return input != null && input.getClass().getName().equals(clazz);
                }
            });
        }

        return defaultProvider;
    }

    /** Fully qualified URL for the Root display URL */
    public String getRoot() {
        String root = getJenkins().getRootUrl();
        if (root == null) {
            root = "http://unconfigured-jenkins-location/";
        }
        return Util.encode(root);
    }

    /** Display name of this provider e.g. "Jenkins Classic", "Blue Ocean", etc */
    public String getDisplayName() {
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

    private static Jenkins getJenkins() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins has not started");
        }
        return jenkins;
    }

    private static final String JENKINS_DISPLAYURL_PROVIDER_ENV = "JENKINS_DISPLAYURL_PROVIDER";
    private static final String JENKINS_DISPLAYURL_PROVIDER_PROP = "jenkins.displayurl.provider";
}
