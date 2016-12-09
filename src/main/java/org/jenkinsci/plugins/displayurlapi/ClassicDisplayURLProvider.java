package org.jenkinsci.plugins.displayurlapi;

import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestObject;

/**
 * Display URL Provider for the Classical Jenkins UI
 */
@Extension
public class ClassicDisplayURLProvider extends DisplayURLProvider {

    @Override
    public String getDisplayName() {
        return Messages.classic_name();
    }

    @Override
    public String getRunURL(Run<?, ?> run) {
        return getRoot() + Util.encode(run.getUrl());
    }

    @Override
    public String getChangesURL(Run<?, ?> run) {
        return getJobURL(run.getParent()) + "changes";
    }

    @Override
    public String getJobURL(Job<?, ?> job) {
        return getRoot() + Util.encode(job.getUrl());
    }

    @Override
    public String getTestUrl(hudson.tasks.test.TestResult result) {
        String buildUrl = getRunURL(result.getRun());
        AbstractTestResultAction action = result.getTestResultAction();

        TestObject parent = result.getParent();
        TestResult testResultRoot = null;
        while(parent != null) {
            if (parent instanceof TestResult) {
                testResultRoot = (TestResult) parent;
                break;
            }
            parent = parent.getParent();
        }

        String testUrl = action.getUrlName()
                + (testResultRoot != null ? testResultRoot.getUrl() : "")
                + result.getUrl();

        String[] pathComponents = testUrl.split("/");
        StringBuilder buf = new StringBuilder();
        for (String c : pathComponents) {
            buf.append(Util.rawEncode(c)).append('/');
        }
        // remove last /
        buf.deleteCharAt(buf.length() - 1);

        return buildUrl + buf.toString();
    }
}
