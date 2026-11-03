package com.github.spacemex.graalscript.runtime;

import com.github.spacemex.graalscript.api.GraalScriptExtension;
import com.github.spacemex.graalscript.api.ScriptApiRegistry;
import com.github.spacemex.graalscript.api.builtin.GraalSystem;
import com.github.spacemex.graalscript.api.builtin.ServerEvents;
import com.github.spacemex.graalscript.api.builtin.event.BuiltinEventRegistrar;
import com.github.spacemex.graalscript.api.event.ScriptEventDispatcher;
import com.github.spacemex.graalscript.api.event.ScriptEventRegistry;
import dev.architectury.event.events.common.LifecycleEvent;

import java.util.ServiceLoader;

public final class GraalScriptBootstrap {

    private static final ScriptApiRegistry REGISTRY = new ScriptApiRegistry();
    private static final ScriptEventRegistry EVENTS = new ScriptEventRegistry();
    private static final GraalScriptRuntime RUNTIME = new GraalScriptRuntime(REGISTRY, EVENTS);
    private static boolean initialized = false;

    private GraalScriptBootstrap() {
    }


    public static synchronized void initializeCommon() {
        if (initialized) {
            return;
        }

        REGISTRY.register(new GraalSystem());

        // Install Minecraft event bridges.
        BuiltinEventRegistrar.register(EVENTS);

        // One binding handles all server events.
        REGISTRY.register(new ServerEvents(EVENTS));

        // Allow third-party mods to extend the registries.
        ServiceLoader.load(GraalScriptExtension.class)
                .forEach(extension -> {
                    extension.registerEvents(EVENTS);
                    extension.register(REGISTRY);
                });

        EVENTS.seal();
        REGISTRY.seal();

        LifecycleEvent.SERVER_STARTED.register(server -> {

            EVENTS.setDispatcher(
                    ScriptScope.SERVER,
                    new ScriptEventDispatcher() {

                        @Override
                        public boolean isEventThread() {
                            return server.isSameThread();
                        }

                        @Override
                        public void execute(Runnable task) {
                            server.execute(task);
                        }
                    }
            );

            RUNTIME.start(ScriptScope.SERVER);
        });

        LifecycleEvent.SERVER_STOPPED.register(server -> {
            EVENTS.clearScope(ScriptScope.SERVER);
            RUNTIME.stop(ScriptScope.SERVER);
        });

        initialized = true;

        RUNTIME.start(ScriptScope.STARTUP);
    }


    public static synchronized void initializeClient() {
        initializeCommon();
        RUNTIME.start(ScriptScope.CLIENT);
    }
}