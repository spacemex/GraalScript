package com.github.spacemex.graalscript.runtime;

public enum ScriptScope {
    STARTUP("startup"),
    SERVER("server"),
    CLIENT("client");

    private final String directory;

    ScriptScope(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }
}
