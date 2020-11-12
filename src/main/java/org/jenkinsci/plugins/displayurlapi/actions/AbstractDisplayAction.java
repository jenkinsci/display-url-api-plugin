package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.annotations.VisibleForTesting;
import hudson.ExtensionList;
import hudson.model.Action;
import hudson.model.User;
import java.util.Objects;
import java.util.function.Predicate;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.displayurlapi.user.PreferredProviderUserProperty;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        if (StringUtils.isNotEmpty(providerName)) {
            ExtensionList<DisplayURLProvider> providers = DisplayURLProvider.all();
            DisplayURLProvider provider = providers.stream()
                .filter(Objects::nonNull)
                .filter(displayURLProvider -> providerName.equals(displayURLProvider.getName()))
                .findFirst()
                .orElse(null);

            if (provider != null) {
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
            ExtensionList<DisplayURLProvider> all = DisplayURLProvider.all();
            displayURLProvider = all.stream()
                .filter(
                    ((Predicate<DisplayURLProvider>) ClassicDisplayURLProvider.class::isInstance)
                        .negate())
                .findFirst().orElse(DisplayURLProvider.getDefault());
        }
        return displayURLProvider;
    }

    @VisibleForTesting
    @Deprecated
    /**
     * @deprecated use {@link DisplayURLProvider#getUserPReferredProviderProperty}
     */
    protected PreferredProviderUserProperty getUserPreferredProviderProperty() {
        return DisplayURLProvider.getUserPreferredProviderProperty();
    }

}
