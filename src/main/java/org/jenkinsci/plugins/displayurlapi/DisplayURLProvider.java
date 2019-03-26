package org.jenkinsci.plugins.displayurlapi;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.actions.AbstractDisplayAction;
import org.jenkinsci.plugins.displayurlapi.user.PreferredProviderUserProperty;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Generates URLs for well known UI locations for use in notifications (e.g. mailer, HipChat, Slack, IRC, etc)
 * Extensible to allow plugins to override common URLs (e.g. Blue Ocean or another future secondary UI)
 */
public abstract class DisplayURLProvider implements ExtensionPoint {

    /**
     * Returns the {@link DisplayURLProvider} to use for generating links to be given to users.
     *
     * @return DisplayURLProvider
     */
    public static DisplayURLProvider get() {
        return DisplayURLProviderImpl.INSTANCE;
    }

    /**
     * Returns all the {@link DisplayURLProvider} implementations.
     *
     * @return all the {@link DisplayURLProvider} implementations.
     */
    public static Iterable<DisplayURLProvider> all() {
        return ExtensionList.lookup(DisplayURLProvider.class);
    }

    public static DisplayURLProvider getDefault() {
        DisplayURLProvider defaultProvider = getPreferredProvider();
        if (defaultProvider == null) {
            defaultProvider = ExtensionList.lookup(DisplayURLProvider.class).get(ClassicDisplayURLProvider.class);
        }
        return defaultProvider;
    }

    /**
     * Fully qualified URL for the Root display URL
     */
    @NonNull
    public String getRoot() {
        String root = Jenkins.getInstance().getRootUrl();
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

    /** Name of provider to be used as an id. Do not use i18n */
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
     * Fully qualified URL for a page that displays changes for a project.
     */
    @NonNull
    public abstract String getChangesURL(Run<?, ?> run);

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
            DisplayURLContext ctx = DisplayURLContext.open();
            try {
                if (ctx.run() == null) {
                    // the link might be generated from another run so we only add this to the context if unset
                    ctx.run(run);
                }
                return DisplayURLDecorator.decorate(ctx, super.getRunURL(run) + DISPLAY_POSTFIX);
            } finally {
                ctx.close();
            }
        }

        @Override
        @NonNull
        public String getChangesURL(Run<?, ?> run) {
            DisplayURLContext ctx = DisplayURLContext.open();
            try {
                if (ctx.run() == null) {
                    // the link might be generated from another run so we only add this to the context if unset
                    ctx.run(run);
                }
                return DisplayURLDecorator.decorate(ctx, super.getRunURL(run) + DISPLAY_POSTFIX + "?page=changes");
            } finally {
                ctx.close();
            }
        }

        @Override
        @NonNull
        public String getJobURL(Job<?, ?> job) {
            DisplayURLContext ctx = DisplayURLContext.open();
            try {
                if (ctx.job() == null) {
                    // the link might be generated from another job so we only add this to the context if unset
                    ctx.job(job);
                }
                return DisplayURLDecorator.decorate(ctx, super.getJobURL(job) + DISPLAY_POSTFIX);
            } finally {
                ctx.close();
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
        String clazz = findClass();
        if (isNotEmpty(clazz)) {
            return ExtensionList.lookup(DisplayURLProvider.class).getDynamic(clazz);
        }
        return null;
    }

    private static final String JENKINS_DISPLAYURL_PROVIDER_ENV = "JENKINS_DISPLAYURL_PROVIDER";
    private static final String JENKINS_DISPLAYURL_PROVIDER_PROP = "jenkins.displayurl.provider";
}
