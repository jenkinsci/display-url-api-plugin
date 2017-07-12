package org.jenkinsci.plugins.displayurlapi;

import hudson.model.Job;
import hudson.model.Run;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.lang.StringUtils;

/**
 * A {@link DisplayURLProvider} that returns URLs with UTM tracking information.
 *
 * @since 2.0
 */
public class UTMDisplayURLProvider extends DisplayURLProvider {
    private final DisplayURLProvider delegate;
    private final String source;
    private final String medium;
    private final String campaign;
    private final String term;
    private final String content;

    UTMDisplayURLProvider(DisplayURLProvider delegate, String medium) {
        this.delegate = delegate;
        this.source = "jenkins";
        this.medium = medium;
        this.campaign = null;
        this.term = null;
        this.content = null;
    }

    private UTMDisplayURLProvider(DisplayURLProvider delegate, String source, String medium, String campaign,
                                  String term, String content) {
        this.delegate = delegate;
        this.source = source;
        this.medium = medium;
        this.campaign = campaign;
        this.term = term;
        this.content = content;
    }

    public UTMDisplayURLProvider withSource(String source) {
        return new UTMDisplayURLProvider(delegate, source, medium, campaign, term, content);
    }

    public UTMDisplayURLProvider withMedium(String medium) {
        return new UTMDisplayURLProvider(delegate, source, medium, campaign, term, content);
    }

    public UTMDisplayURLProvider withCampaign(String campaign) {
        return new UTMDisplayURLProvider(delegate, source, medium, campaign, term, content);
    }

    public UTMDisplayURLProvider withTerm(String term) {
        return new UTMDisplayURLProvider(delegate, source, medium, campaign, term, content);
    }

    public UTMDisplayURLProvider withContent(String content) {
        return new UTMDisplayURLProvider(delegate, source, medium, campaign, term, content);
    }

    @Override
    public String getRoot() {
        return applyUTM(delegate.getRoot());
    }

    @Override
    public String getRunURL(Run<?, ?> run) {
        return applyUTM(delegate.getRunURL(run));
    }

    @Override
    public String getChangesURL(Run<?, ?> run) {
        return applyUTM(delegate.getChangesURL(run));
    }

    @Override
    public String getJobURL(Job<?, ?> job) {
        return applyUTM(delegate.getJobURL(job));
    }

    private String applyUTM(String url) {
        StringBuilder result = new StringBuilder();
        char sep = '?';
        int queryStart = url.indexOf(sep);
        if (queryStart == -1) {
            // quick win!
            result.append(url);
        } else {
            // ok this is the hard one, we need to build up the url and strip any utm_ query parameters
            result.append(url.substring(0, queryStart));
            for (String pair : url.substring(queryStart + 1).split("&")) {
                if ((pair.startsWith("utm_source=") || pair.equals("utm_source"))
                        || (pair.startsWith("utm_medium=") || pair.equals("utm_medium"))
                        || (pair.startsWith("utm_campaign=") || pair.equals("utm_campaign"))
                        || (pair.startsWith("utm_term=") || pair.equals("utm_term"))
                        || (pair.startsWith("utm_content=") || pair.equals("utm_content"))) {
                    continue;
                }
                result.append(sep);
                result.append(pair);
                sep = '&';
            }
        }
        if (StringUtils.isNotBlank(source)) {
            result.append(sep).append("utm_source=").append(encode(source));
            sep = '&';
        }
        if (StringUtils.isNotBlank(medium)) {
            result.append(sep).append("utm_medium=").append(encode(medium));
            sep = '&';
        }
        if (StringUtils.isNotBlank(campaign)) {
            result.append(sep).append("utm_campaign=").append(encode(campaign));
            sep = '&';
        }
        if (StringUtils.isNotBlank(term)) {
            result.append(sep).append("utm_term=").append(encode(term));
            sep = '&';
        }
        if (StringUtils.isNotBlank(content)) {
            result.append(sep).append("utm_content=").append(encode(content));
        }
        return result.toString();
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            AssertionError ae = new AssertionError("UTF-8 encoding is mandated by the JLS");
            ae.initCause(e);
            throw ae;
        }
    }
}
