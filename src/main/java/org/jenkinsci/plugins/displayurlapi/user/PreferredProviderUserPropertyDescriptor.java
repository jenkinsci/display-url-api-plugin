package org.jenkinsci.plugins.displayurlapi.user;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import org.jenkinsci.plugins.displayurlapi.Messages;
import org.jenkinsci.plugins.displayurlapi.user.PreferredProviderUserProperty.ProviderOption;

public class PreferredProviderUserPropertyDescriptor extends UserPropertyDescriptor {

    public PreferredProviderUserPropertyDescriptor() {
        super(PreferredProviderUserProperty.class);
    }

    @Override
    public UserProperty newInstance(User user) {
        return new PreferredProviderUserProperty(ProviderOption.DEFAULT_OPTION.getId());
    }

    @Override
    @NonNull
    public String getDisplayName() {
        return Messages.display_url();
    }
}
