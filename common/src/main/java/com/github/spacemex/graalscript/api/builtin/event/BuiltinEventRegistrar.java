
package com.github.spacemex.graalscript.api.builtin.event;

import com.github.spacemex.graalscript.api.event.ScriptEventRegistry;
import com.github.spacemex.graalscript.runtime.ScriptScope;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;

public final class BuiltinEventRegistrar {

    private BuiltinEventRegistrar() {
    }

    public static void register(ScriptEventRegistry events) {

        events.register(
                "graalscript:player_join",
                ScriptScope.SERVER,
                PlayerJoinEvent.class,
                event -> PlayerEvent.PLAYER_JOIN.register(player -> {
                    event.fire(new PlayerJoinEvent(player));
                })
        );

        events.register(
                "graalscript:attack_entity",
                ScriptScope.SERVER,
                EntityAttackEvent.class,
                event -> PlayerEvent.ATTACK_ENTITY.register((player, level, target, hand, hitResult) -> {
                    if (!(player instanceof ServerPlayer)) {
                        return EventResult.pass();
                    }

                    EntityAttackEvent payload = new EntityAttackEvent((ServerPlayer) player, level, target, hand, hitResult,
                            BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());

                    event.fireSync(payload);
                    return payload.isCancelled() ? EventResult.interruptFalse() : EventResult.pass();
                })
        );
    }
}
