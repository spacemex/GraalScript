package com.github.spacemex.graalscript;

import com.github.spacemex.graalscript.runtime.GraalScriptBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GraalScript {
    public static final String MOD_ID = "graal_script";

    public static void CommonInitialize() {
        GraalScriptBootstrap.initializeCommon();
    }

    public static void ClientInitialize() {
        GraalScriptBootstrap.initializeClient();
    }
}
