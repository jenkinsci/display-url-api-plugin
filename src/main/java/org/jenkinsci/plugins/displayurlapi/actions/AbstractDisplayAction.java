package org.jenkinsci.plugins.displayurlapi.actions;

import hudson.ExtensionList;
import hudson.model.Action;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

import jakarta.servlet.http.HttpServletResponse;
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

    public final Object doRedirect(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException {
        DisplayURLProvider provider = lookupProvider(req);
        rsp.sendRedirect(HttpServletResponse.SC_MOVED_TEMPORARILY, getRedirectURL(provider));
        return null;
    }

    protected abstract String getRedirectURL(DisplayURLProvider provider);

    DisplayURLProvider lookupProvider(StaplerRequest2 req) {
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
        return DisplayURLProvider.getPreferredProvider();
    }

}
