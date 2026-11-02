package com.github.spacemex.graalscript;

import net.fabricmc.api.ModInitializer;

public class GraalScriptFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        GraalScript.CommonInitialize();
    }
}
