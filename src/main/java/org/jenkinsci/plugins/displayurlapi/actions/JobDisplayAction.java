package org.jenkinsci.plugins.displayurlapi.actions;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

import java.util.Collection;

public class JobDisplayAction extends AbstractDisplayAction {

    private final Job job;

    JobDisplayAction(Job job) {
        this.job = job;
    }

    protected String getRedirectURL(DisplayURLProvider provider) {
        return provider.getJobURL(job);
    }

    @Extension
    public static class TransientActionFactoryImpl extends TransientActionFactory {

        @Override
        public Class type() {
            return Job.class;
        }

        @NonNull
        @Override
        public Collection<? extends Action> createFor(@NonNull Object target) {
            return ImmutableList.of(new JobDisplayAction((Job) target));
        }
    }
}
