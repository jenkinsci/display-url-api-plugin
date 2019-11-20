package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

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
        if ("changes".equals(page)) {
            url = provider.getChangesURL(run);
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

        @NonNull
        @Override
        public Collection<? extends Action> createFor(@NonNull Object target) {
            return ImmutableList.of(new RunDisplayAction((Run) target));
        }
    }
}
