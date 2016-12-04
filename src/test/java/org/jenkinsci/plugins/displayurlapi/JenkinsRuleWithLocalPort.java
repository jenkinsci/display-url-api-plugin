package org.jenkinsci.plugins.displayurlapi;

import org.jvnet.hudson.test.JenkinsRule;

/**
 * {@link JenkinsRule} that exposes the local port number
 */
public class JenkinsRuleWithLocalPort extends JenkinsRule {
    public int getLocalPort() {
        return this.localPort;
    }
}
