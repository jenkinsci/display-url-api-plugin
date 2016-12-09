package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import hudson.model.Action;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
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
        DisplayURLProvider provider = lookupProvider();
        rsp.sendRedirect(HttpServletResponse.SC_MOVED_TEMPORARILY, getRedirectURL(provider));
        return null;
    }

    protected abstract String getRedirectURL(DisplayURLProvider provider);

    DisplayURLProvider lookupProvider() {
        Iterable<DisplayURLProvider> all = DisplayURLProvider.all();
        DisplayURLProvider defaultProvider = Iterables.find(all, Predicates.instanceOf(ClassicDisplayURLProvider.class));
        Iterable<DisplayURLProvider> availableProviders = Iterables.filter(all, Predicates.not(Predicates.instanceOf(ClassicDisplayURLProvider.class)));
        return Iterables.getFirst(availableProviders, defaultProvider);
    }
}
