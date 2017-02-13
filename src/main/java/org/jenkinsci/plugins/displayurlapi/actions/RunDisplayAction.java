package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
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
        if ("changes".equals(page)) {
            url = provider.getChangesURL(run);
        } else if ("test".equals(page)) {
            String id = req.getParameter("id");
            if (id == null) {
                throw new IllegalArgumentException("id parameter not specified");
            }
            AbstractTestResultAction action = run.getAction(AbstractTestResultAction.class);
            if (action == null) {
                throw new IllegalStateException("No AbstractTestResultAction on this run");
            }
            TestResult result = action.findCorrespondingResult(id);
            url = provider.getTestUrl(result);
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
