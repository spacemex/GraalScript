package com.github.spacemex.graalscript.client;

import com.github.spacemex.graalscript.GraalScript;
import net.fabricmc.api.ClientModInitializer;

public class GraalScriptFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GraalScript.ClientInitialize();
    }
}
