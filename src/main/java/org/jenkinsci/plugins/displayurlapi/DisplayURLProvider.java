package org.jenkinsci.plugins.displayurlapi;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.User;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.actions.AbstractDisplayAction;
import org.jenkinsci.plugins.displayurlapi.user.PreferredProviderUserProperty;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Generates URLs for well known UI locations for use in notifications (e.g. mailer, HipChat, Slack,
 * IRC, etc) Extensible to allow plugins to override common URLs (e.g. Blue Ocean or another future
 * secondary UI)
 */
public abstract class DisplayURLProvider implements ExtensionPoint {

    /**
     * Returns the {@link DisplayURLProvider} to use for generating links to be given to users.
     *
     * @return DisplayURLProvider
     */
    public static DisplayURLProvider get() {
        DisplayURLProvider preferredProvider = getPreferredProvider();
        return preferredProvider != null ? preferredProvider : DisplayURLProviderImpl.INSTANCE;
    }

    /**
     * Returns all the {@link DisplayURLProvider} implementations.
     *
     * @return all the {@link DisplayURLProvider} implementations.
     */
    public static ExtensionList<DisplayURLProvider> all() {
        return ExtensionList.lookup(DisplayURLProvider.class);
    }

    public static DisplayURLProvider getDefault() {
        DisplayURLProvider defaultProvider = getPreferredProvider();
        if (defaultProvider == null) {
            defaultProvider = ExtensionList.lookup(DisplayURLProvider.class)
                .get(ClassicDisplayURLProvider.class);
        }
        return defaultProvider;
    }

    /**
     * Fully qualified URL for the Root display URL
     */
    @NonNull
    public String getRoot() {
        String root = Jenkins.get().getRootUrl();
        if (root == null) {
            root = "http://unconfigured-jenkins-location/";
        }
        return Util.encode(root);
    }

    /**
     * Display name of this provider e.g. "Jenkins Classic", "Blue Ocean", etc
     */
    @NonNull
    public String getDisplayName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Name of provider to be used as an id. Do not use i18n
     */
    @NonNull
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Fully qualified URL for a Run
     */
    @NonNull
    public abstract String getRunURL(Run<?, ?> run);

    /**
     * Fully qualified URL for a page that displays artifacts for a Run.
     */
    @NonNull
    public String getArtifactsURL(Run<?, ?> run) {
        return getRunURL(run) + "artifact";
    }

    /**
     * Fully qualified URL for a page that displays changes for a project.
     */
    @NonNull
    public abstract String getChangesURL(Run<?, ?> run);

    /**
     * Fully qualified URL for a page that displays tests for a Run.
     */
    public abstract String getTestsURL(Run<?, ?> run);

    /**
     * Fully qualified URL for a Jobs home
     */
    @NonNull
    public abstract String getJobURL(Job<?, ?> job);

    /**
     * Generates the URLs that the end user will click on, these URLs will direct to a {@link AbstractDisplayAction}
     * which is then responsible for sending the user to their actual {@link DisplayURLProvider} URL.
     */
    static class DisplayURLProviderImpl extends ClassicDisplayURLProvider {

        static final DisplayURLProvider INSTANCE = new DisplayURLProviderImpl();

        static final String DISPLAY_POSTFIX = AbstractDisplayAction.URL_NAME + "/redirect";

        @Override
        @NonNull
        public String getRunURL(Run<?, ?> run) {
            try (DisplayURLContext ctx = DisplayURLContext.open()) {
                if (ctx.run() == null) {
                    // the link might be generated from another run so we only add this to the context if unset
                    ctx.run(run);
                }
                return DisplayURLDecorator.decorate(ctx, super.getRunURL(run) + DISPLAY_POSTFIX);
            }
        }

        @NonNull
        private String getPageURL(Run<?, ?> run, String page) {
            try (DisplayURLContext ctx = DisplayURLContext.open()) {
                if (ctx.run() == null) {
                    // the link might be generated from another run so we only add this to the context if unset
                    ctx.run(run);
                }
                return DisplayURLDecorator
                    .decorate(ctx, super.getRunURL(run) + DISPLAY_POSTFIX + "?page=" + page);
            }
        }

        @Override
        @NonNull
        public String getArtifactsURL(Run<?, ?> run) {
            return getPageURL(run, "artifacts");
        }

        @Override
        @NonNull
        public String getChangesURL(Run<?, ?> run) {
            return getPageURL(run, "changes");
        }

        @Override
        @NonNull
        public String getTestsURL(Run<?, ?> run) {
            return getPageURL(run, "tests");
        }

        @Override
        @NonNull
        public String getJobURL(Job<?, ?> job) {
            try (DisplayURLContext ctx = DisplayURLContext.open()) {
                if (ctx.job() == null) {
                    // the link might be generated from another job so we only add this to the context if unset
                    ctx.job(job);
                }
                return DisplayURLDecorator.decorate(ctx, super.getJobURL(job) + DISPLAY_POSTFIX);
            }
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
        PreferredProviderUserProperty prefProperty = getUserPreferredProviderProperty();

        if (prefProperty != null && prefProperty.getConfiguredProvider() != null) {
            return prefProperty.getConfiguredProvider();
        }
        String clazz = findClass();
        if (isNotEmpty(clazz)) {
            return ExtensionList.lookup(DisplayURLProvider.class).getDynamic(clazz);
        }
        return null;
    }

    @Nullable
    public static PreferredProviderUserProperty getUserPreferredProviderProperty() {
        User user = User.current();
        return (user == null) ? null : user.getProperty(PreferredProviderUserProperty.class);
    }

    private static final String JENKINS_DISPLAYURL_PROVIDER_ENV = "JENKINS_DISPLAYURL_PROVIDER";
    private static final String JENKINS_DISPLAYURL_PROVIDER_PROP = "jenkins.displayurl.provider";
}
