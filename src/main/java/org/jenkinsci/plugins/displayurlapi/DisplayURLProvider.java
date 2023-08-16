package org.jenkinsci.plugins.displayurlapi;

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
 *
 * <p>Implementations should generally extend {@link ClassicDisplayURLProvider} and delegate to it for unsupported
 * builds instead of extending this class directly.
 */
/* TODO: This API is awkward. All providers must return a non-null value for all URLs, meaning that in practice they
   all need to delegate to ClassicDisplayURLProvider. Ideally, the API would allow providers to return null to indicate
   that they do not support a particular build/job, and we would loop over all providers in a user-defined order looking
   for the first one that returns a non-null URL, which would allow providers which handle distinct job types to coexist.
*/
public abstract class DisplayURLProvider implements ExtensionPoint {

    /**
     * Returns the {@link DisplayURLProvider} to use for generating links to be given to users.
     *
     * @return DisplayURLProvider
     * @see #getPreferredProvider
     */
    public static DisplayURLProvider get() {
        return DisplayURLProviderImpl.INSTANCE;
    }

    /**
     * Returns all the {@link DisplayURLProvider} implementations.
     *
     * @return all the {@link DisplayURLProvider} implementations.
     */
    public static ExtensionList<DisplayURLProvider> all() {
        return ExtensionList.lookup(DisplayURLProvider.class);
    }

    /**
     * Returns the singleton instance of the {@link ClassicDisplayURLProvider} extension.
     */
    public static DisplayURLProvider getDefault() {
        return ExtensionList.lookup(DisplayURLProvider.class)
                .get(ClassicDisplayURLProvider.class);
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

    /**
     * Selects the provider that should be used to redirect users from the display URLs generated by
     * the provider return by {@link #get} to an actual page.
     * <p>Precedence is as follows, stopping at the first non-null value:
     * <ol>
     * <li>{@link PreferredProviderUserProperty}
     * <li>{@link DefaultProviderGlobalConfiguration}
     * <li>{@link #JENKINS_DISPLAYURL_PROVIDER_ENV}
     * <li>{@link #JENKINS_DISPLAYURL_PROVIDER_PROP}
     * <li>The provider extension with the highest ordinal value that is not an instance of {@link ClassicDisplayURLProvider}
     * <li>{@link ClassicDisplayURLProvider}
     * </ol>
     * @see #get
     */
    @Nullable
    public static DisplayURLProvider getPreferredProvider() {
        PreferredProviderUserProperty userProperty = getUserPreferredProviderProperty();
        if (userProperty != null && userProperty.getConfiguredProvider() != null) {
            return userProperty.getConfiguredProvider();
        }
        DisplayURLProvider globalGuiProvider = DefaultProviderGlobalConfiguration.get().getConfiguredProvider();
        if (globalGuiProvider != null) {
            return globalGuiProvider;
        }
        String globalProviderClass = findClass();
        if (isNotEmpty(globalProviderClass)) {
            return ExtensionList.lookup(DisplayURLProvider.class).getDynamic(globalProviderClass);
        }
        ExtensionList<DisplayURLProvider> all = DisplayURLProvider.all();
        DisplayURLProvider displayURLProvider = all.stream()
                .filter(p -> !(p instanceof ClassicDisplayURLProvider))
                .findFirst().orElse(DisplayURLProvider.getDefault());
        return displayURLProvider;
    }

    @Nullable
    public static PreferredProviderUserProperty getUserPreferredProviderProperty() {
        User user = User.current();
        return (user == null) ? null : user.getProperty(PreferredProviderUserProperty.class);
    }

    static final String JENKINS_DISPLAYURL_PROVIDER_ENV = "JENKINS_DISPLAYURL_PROVIDER";
    static final String JENKINS_DISPLAYURL_PROVIDER_PROP = "jenkins.displayurl.provider";
}
