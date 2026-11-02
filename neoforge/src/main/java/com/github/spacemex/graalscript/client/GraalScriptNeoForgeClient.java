package com.github.spacemex.graalscript.client;

import com.github.spacemex.graalscript.GraalScript;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = GraalScript.MOD_ID, dist = Dist.CLIENT)
public final class GraalScriptNeoForgeClient {

    public GraalScriptNeoForgeClient(IEventBus eventBus) {
        GraalScript.ClientInitialize();
    }
}
