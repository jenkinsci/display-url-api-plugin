package org.jenkinsci.plugins.displayurlapi.user;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.util.ListBoxModel;
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

    public ListBoxModel doFillProviderIdItems() {
        ListBoxModel items = new ListBoxModel();
        PreferredProviderUserProperty property = PreferredProviderUserProperty.forCurrentUser();
        for (ProviderOption providerOption : property.getAll()) {
            ListBoxModel.Option option = new ListBoxModel.Option(
                    providerOption.getName(),
                    providerOption.getId(),
                    property.isSelected(providerOption.getId())
            );
            items.add(option);
        }
        return items;
    }

    @Override
    @NonNull
    public String getDisplayName() {
        return Messages.display_url();
    }
}
