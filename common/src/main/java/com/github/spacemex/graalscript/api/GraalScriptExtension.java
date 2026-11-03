package com.github.spacemex.graalscript.api;

import com.github.spacemex.graalscript.api.event.ScriptEventRegistry;

public interface GraalScriptExtension {

    void register(ScriptApiRegistry registry);

    default void registerEvents(ScriptEventRegistry registry) {}
}
