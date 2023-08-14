/*
 * The MIT License
 *
 * Copyright 2023 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.displayurlapi;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.util.ListBoxModel;
import java.util.Objects;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.plugins.displayurlapi.user.PreferredProviderUserProperty;
import org.kohsuke.stapler.DataBoundSetter;

@Extension
public class DefaultProviderGlobalConfiguration extends GlobalConfiguration {

    public DefaultProviderGlobalConfiguration() {
        load();
    }

    private @CheckForNull String providerId;

    public @CheckForNull String getProviderId() {
        return providerId;
    }

    @DataBoundSetter
    public void setProviderId(@CheckForNull String providerId) {
        providerId = Util.fixEmptyAndTrim(providerId);
        if (PreferredProviderUserProperty.ProviderOption.DEFAULT_OPTION.getId().equals(providerId)) {
            providerId = null;
        }
        this.providerId = providerId;
        save();
    }

    public @CheckForNull DisplayURLProvider getConfiguredProvider() {
        if (providerId == null) {
            return null;
        }
        return DisplayURLProvider.all().stream()
            .filter(provider -> provider.getClass().getName().equals(providerId))
            .findFirst()
            .orElse(null);
    }

    public static DefaultProviderGlobalConfiguration get() {
        return ExtensionList.lookupSingleton(DefaultProviderGlobalConfiguration.class);
    }

    public ListBoxModel doFillProviderIdItems() {
        ListBoxModel items = new ListBoxModel();
        for (PreferredProviderUserProperty.ProviderOption providerOption : PreferredProviderUserProperty.getAll()) {
            ListBoxModel.Option option = new ListBoxModel.Option(
                    providerOption.getName(),
                    providerOption.getId(),
                    Objects.equals(providerOption.getId(), providerId)
            );
            items.add(option);
        }
        return items;
    }
}
