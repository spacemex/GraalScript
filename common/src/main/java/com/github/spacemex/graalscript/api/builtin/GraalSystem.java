package com.github.spacemex.graalscript.api.builtin;

import com.github.spacemex.graalscript.api.annotation.ScriptBinding;
import com.github.spacemex.graalscript.api.annotation.ScriptMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ScriptBinding(name = "Graal")
public final class GraalSystem {

    private static final Logger LOGGER = LoggerFactory.getLogger("GraalScript");

    @ScriptMethod(description = "Logs a message.")
    public void log(String message) {
        LOGGER.info("[Script] {}", message);
    }

    @ScriptMethod(description = "Logs a warning.")
    public void warn(String message) {
        LOGGER.warn("[Script] {}", message);
    }

    @ScriptMethod(description = "Logs an error.")
    public void error(String message) {
        LOGGER.error("[Script] {}", message);
    }
}
