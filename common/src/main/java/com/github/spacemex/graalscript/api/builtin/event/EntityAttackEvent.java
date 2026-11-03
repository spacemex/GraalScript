package com.github.spacemex.graalscript.api.builtin.event;

import com.github.spacemex.graalscript.api.annotation.ScriptMethod;
import com.github.spacemex.graalscript.api.annotation.ScriptType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

@ScriptType(name = "EntityAttackEvent")
public class EntityAttackEvent {

    private final ServerPlayer player;
    private final Entity entity;
    private final InteractionHand hand;
    private final Level level;
    private final EntityHitResult blockHitResult;

    private boolean cancelled;

    public EntityAttackEvent(ServerPlayer player, Level level, Entity target, InteractionHand hand, EntityHitResult hitResult) {
        this.player = player;
        this.level = level;
        this.entity = target;
        this.hand = hand;
        this.blockHitResult = hitResult;
    }

    @ScriptMethod(description = "Get the entities identifier")
    public String getEntityId() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    @ScriptMethod(description = "Get the hit entity")
    public Entity getEntity() {
        return entity;
    }

    @ScriptMethod(description = "Cancels the attack")
    public void cancel() {
        cancelled = true;
    }

    @ScriptMethod(description = "Checks weather the event was cancelled")
    public boolean isCancelled() {
        return cancelled;
    }
}
