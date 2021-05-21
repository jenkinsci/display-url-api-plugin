package org.jenkinsci.plugins.displayurlapi.user;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

import java.util.stream.Collectors;

import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

public class PreferredProviderUserProperty extends UserProperty {

    @Extension
    public static final UserPropertyDescriptor DESCRIPTOR = new PreferredProviderUserPropertyDescriptor();

    @Nullable
    private String providerId;

    @DataBoundConstructor
    public PreferredProviderUserProperty(@Nullable String providerId) {
        this.providerId = providerId;
    }

    public ProviderOption getProvider() {
        final DisplayURLProvider provider = getConfiguredProvider();
        return provider == null ? ProviderOption.DEFAULT_OPTION
            : new ProviderOption(provider.getClass().getName(), provider.getDisplayName());
    }

    public static PreferredProviderUserProperty forCurrentUser() {
        final User current = User.current();
        if (current == null) {
            return (PreferredProviderUserProperty) DESCRIPTOR.newInstance((User) null);
        }

        PreferredProviderUserProperty property = current.getProperty(PreferredProviderUserProperty.class);
        if (property == null) {
            return (PreferredProviderUserProperty) DESCRIPTOR.newInstance(current);
        }
        return property;
    }

    public DisplayURLProvider getConfiguredProvider() {
        return DisplayURLProvider.all().stream()
            .filter(input -> input.getClass().getName().equals(providerId))
            .findFirst()
            .orElse(null);
    }

    public List<ProviderOption> getAll() {
        List<ProviderOption> options = DisplayURLProvider.all().stream()
            .map(input -> new ProviderOption(input.getClass().getName(), input.getDisplayName()))
            .collect(Collectors.toList());
        return ImmutableList.<ProviderOption>builder()
            .add(ProviderOption.DEFAULT_OPTION).addAll(options).build();
    }

    public boolean isSelected(String providerId) {
        return getProvider().getId().equals(providerId);
    }

    public static class ProviderOption {

        public static final ProviderOption DEFAULT_OPTION = new ProviderOption("default", "Default");

        private final String id;
        private final String name;

        public ProviderOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
