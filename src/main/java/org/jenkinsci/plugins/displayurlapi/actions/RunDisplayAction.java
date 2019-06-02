package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.Collection;

public class RunDisplayAction extends AbstractDisplayAction {
    private final Run run;

    protected RunDisplayAction(Run run) {
        this.run = run;
    }

    @Override
    protected String getRedirectURL(DisplayURLProvider provider) {
        StaplerRequest req = Stapler.getCurrentRequest();
        String page = req.getParameter("page");
        String url;
        if ("artifacts".equals(page)) {
            url = provider.getArtifactsURL(run);
        } else if ("changes".equals(page)) {
            url = provider.getChangesURL(run);
        } else if ("tests".equals(page)) {
            url = provider.getTestsURL(run);
        } else {
            url = provider.getRunURL(run);
        }
        return url;
    }

    @Extension
    public static class TransientActionFactoryImpl extends TransientActionFactory {
        @Override
        public Class type() {
            return Run.class;
        }

        @Nonnull
        @Override
        public Collection<? extends Action> createFor(@Nonnull Object target) {
            return ImmutableList.of(new RunDisplayAction((Run) target));
        }
    }
}
