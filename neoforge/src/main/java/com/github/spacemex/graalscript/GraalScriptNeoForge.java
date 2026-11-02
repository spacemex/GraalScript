package com.github.spacemex.graalscript;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(GraalScript.MOD_ID)
public class GraalScriptNeoForge {

    public GraalScriptNeoForge(IEventBus eventBus) {
        GraalScript.CommonInitialize();
    }
}
