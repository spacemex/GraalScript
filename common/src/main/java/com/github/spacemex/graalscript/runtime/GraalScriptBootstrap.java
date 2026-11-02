package com.github.spacemex.graalscript.runtime;

import com.github.spacemex.graalscript.api.GraalScriptExtension;
import com.github.spacemex.graalscript.api.ScriptApiRegistry;
import com.github.spacemex.graalscript.api.builtin.GraalSystem;
import com.github.spacemex.graalscript.api.builtin.ServerEvents;
import com.github.spacemex.graalscript.api.builtin.event.EntityAttackEvent;
import com.github.spacemex.graalscript.api.builtin.event.PlayerJoinEvent;
import com.github.spacemex.graalscript.api.event.ScriptEvent;
import com.github.spacemex.graalscript.api.event.ScriptEventDispatcher;
import com.github.spacemex.graalscript.api.event.ScriptEventRegistry;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;

import java.util.ServiceLoader;

public final class GraalScriptBootstrap {

    private static final ScriptApiRegistry REGISTRY = new ScriptApiRegistry();
    private static final GraalScriptRuntime RUNTIME = new GraalScriptRuntime(REGISTRY);
    private static final ScriptEventRegistry EVENTS = new ScriptEventRegistry();
    private static final ScriptEvent<PlayerJoinEvent> PLAYER_JOIN = EVENTS.register("graalscript:player_join", ScriptScope.SERVER, PlayerJoinEvent.class);
    private static final ScriptEvent<EntityAttackEvent> ATTACK_ENTITY = EVENTS.register("graalscript:attack_entity", ScriptScope.SERVER, EntityAttackEvent.class);
    private static boolean initialized = false;

    private GraalScriptBootstrap() {
    }


    public static synchronized void initializeCommon() {
        if (initialized) {
            return;
        }

        REGISTRY.register(new GraalSystem());

        REGISTRY.register(new ServerEvents(
                PLAYER_JOIN,
                ATTACK_ENTITY
        ));

        ServiceLoader.load(GraalScriptExtension.class)
                .forEach(extension -> {
                    extension.registerEvents(EVENTS);
                    extension.register(REGISTRY);
                });

        EVENTS.seal();
        REGISTRY.seal();

        // Notification event.
        PlayerEvent.PLAYER_JOIN.register(player -> {
            PLAYER_JOIN.fire(new PlayerJoinEvent(
                    player.getName().getString(),
                    player.getUUID().toString()
            ));
        });

        // Cancellable event.
        PlayerEvent.ATTACK_ENTITY.register((
                player,
                level,
                target,
                hand,
                hitResult
        ) -> {
            // Only dispatch on the logical server.
            if (!(player instanceof ServerPlayer)) {
                return EventResult.pass();
            }

            EntityAttackEvent event = new EntityAttackEvent(
                    player.getName().getString(),
                    BuiltInRegistries.ENTITY_TYPE
                            .getKey(target.getType())
                            .toString()
            );

            ATTACK_ENTITY.fireSync(event);

            return event.isCancelled()
                    ? EventResult.interruptFalse()
                    : EventResult.pass();
        });

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