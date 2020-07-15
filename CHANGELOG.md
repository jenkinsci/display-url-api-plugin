# Changelog

From 2.3.3 please look at (https://github.com/jenkinsci/display-url-api-plugin/releases)

## 2.3.2 - 3 August 2019

-   [JENKINS-58654](https://issues.jenkins-ci.org/browse/JENKINS-58654): Improve performance of display-url-api-plugin (`DisplayURLContext` class).

## 2.3.1 - 26 March 2019

-   [PR 24](https://github.com/jenkinsci/display-url-api-plugin/pull/24): Update the plugin's dependencies so it can build successfully on Java 11.
-   [PR 22](https://github.com/jenkinsci/display-url-api-plugin/pull/22): Minor grammar and spelling fixes.

## 2.3.0 - 16 November 2018

-   [JENKINS-45478](https://issues.jenkins-ci.org/browse/JENKINS-45478) Add an API that will allow extension plugins to decorate the clickable links additional parameters based on the context within which the link was generated.

## 2.2.0 – 28 November 2017

-   [JENKINS-41502](https://issues.jenkins-ci.org/browse/JENKINS-41502) Select provider using a parameter

## 2.1.0 – 3 October 2017

-   [JENKINS-41578](https://issues.jenkins-ci.org/browse/JENKINS-41578) Add a system property/environment variable to specify default display url implementation

## 2.0 – 14 April 2017

-   [JENKINS-43538](https://issues.jenkins-ci.org/browse/JENKINS-43538) API changes: `DisplayURLProvider#getTestUrl()` and `DisplayURLProvider#getTestURL()` was removed to break dependency on JUnit plugin
    -   There is no known plugins using this API
    -   If the API is being used via System Groovy or Pipeline scripts, these scrips will need to be updated after the release

## 1.1.1 – 3 Feb 2017

-   Depend on JUnit 1.19 as previous much older version of this plugin appears not to be available in all update center mirrors, causing installation failure.

## 1.1 – 3 Feb 2017

-   [JENKINS-41677](https://issues.jenkins-ci.org/browse/JENKINS-41677) Expose display URLs in environment variables
    -   **RUN\_DISPLAY\_URL** – links to the run result
    -   **RUN\_CHANGES\_DISPLAY\_URL** – links to the changes page for a run
    -   **JOB\_DISPLAY\_URL** – links to the jobs homepage

## 1.0.1 – 31 Jan 2017

-   [JENKINS-41578](https://issues.jenkins-ci.org/browse/JENKINS-41578) Add a system property/environment variable to specify default display url imp

## 1.0 – 30 Jan 2017

-   User can prefer Jenkins Classic or Blue Ocean URLs by changing a preference in their user profile

## 0.5 – 22 Sept 2016

-   API update: Move getRoot() to super class so that implementors can use the default root calculation

## 0.4 – 22 Sept 2016

-   [JENKINS-38067](https://issues.jenkins-ci.org/browse/JENKINS-38067) encode URLs with Util.encode

## 0.3 – 8 Sept 2016

-   If the root URL cannot be resolved fall back on "http://unconfigured-jenkins-location/"
-   Improve test coverage

## 0.2 – 1 Sept 2016

-   Ensure that folder and job names are encoded
-   Fix root URL postfix slash

## 0.1 – 1 Sept 2016

-   Initial version
