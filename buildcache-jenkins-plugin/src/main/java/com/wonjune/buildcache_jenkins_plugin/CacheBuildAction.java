package com.wonjune.buildcache_jenkins_plugin;

import hudson.model.Action;
import hudson.model.Run;

public class CacheBuildAction implements Action {
    private final Run<?, ?> build;
    private final String cachedResult;

    public CacheBuildAction(Run<?, ?> build, String cachedResult) {
        this.build = build;
        this.cachedResult = cachedResult;
    }

    public String getCachedResult() {
        return cachedResult;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Cached Build Result";
    }

    @Override
    public String getUrlName() {
        return "cachedResult";
    }
}
