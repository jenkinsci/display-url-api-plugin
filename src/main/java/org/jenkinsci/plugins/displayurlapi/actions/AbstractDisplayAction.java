package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import hudson.model.Action;
import hudson.model.User;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.displayurlapi.user.PreferredProviderUserProperty;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Predicate;

public abstract class AbstractDisplayAction implements Action {

    public static final String URL_NAME = "display";

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }

    public final Object doRedirect(StaplerRequest req, StaplerResponse rsp) throws IOException {
        DisplayURLProvider provider = lookupProvider(req);
        rsp.sendRedirect(HttpServletResponse.SC_MOVED_TEMPORARILY, getRedirectURL(provider));
        return null;
    }

    protected abstract String getRedirectURL(DisplayURLProvider provider);

    DisplayURLProvider lookupProvider(StaplerRequest req) {
        final String providerName = req.getParameter("provider");
        if(providerName != null && !providerName.isEmpty()) {
            Iterable<DisplayURLProvider> providers = DisplayURLProvider.all();
            Iterable<DisplayURLProvider> filtered = Iterables.filter(providers, new com.google.common.base.Predicate<DisplayURLProvider>() {
                @Override
                public boolean apply(@Nullable DisplayURLProvider displayURLProvider) {
                    if(displayURLProvider == null) {
                        return false;
                    }

                    return displayURLProvider.getName().equals(providerName);
                }
            });

            DisplayURLProvider provider = Iterables.getFirst(filtered, null);
            if(provider != null) {
                return provider;
            }
        }

        return lookupProvider();
    }
    DisplayURLProvider lookupProvider() {
        PreferredProviderUserProperty prefProperty = getUserPreferredProviderProperty();

        if (prefProperty != null && prefProperty.getConfiguredProvider() != null) {
            return prefProperty.getConfiguredProvider();
        }
        DisplayURLProvider displayURLProvider = DisplayURLProvider.getPreferredProvider();
        if (displayURLProvider == null) {
            Iterable<DisplayURLProvider> all = DisplayURLProvider.all();
            Iterable<DisplayURLProvider> availableProviders = Iterables.filter(all, Predicates.not(Predicates.instanceOf(ClassicDisplayURLProvider.class)));
            displayURLProvider = Iterables.getFirst(availableProviders, DisplayURLProvider.getDefault());
        }
        return displayURLProvider;
    }

    @VisibleForTesting
    protected PreferredProviderUserProperty getUserPreferredProviderProperty() {
        User user = User.current();
        return (user == null) ? null : user.getProperty(PreferredProviderUserProperty.class);
    }

}
