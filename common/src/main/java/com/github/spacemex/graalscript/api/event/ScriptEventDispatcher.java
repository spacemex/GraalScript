package com.github.spacemex.graalscript.api.event;

public interface ScriptEventDispatcher {
    boolean isEventThread();

    void execute(Runnable task);
}
