package com.github.spacemex.graalscript.api.builtin.event;

import com.github.spacemex.graalscript.api.annotation.ScriptMethod;
import com.github.spacemex.graalscript.api.annotation.ScriptType;

@ScriptType(name = "EntityAttackEvent")
public class EntityAttackEvent {

    private final String playerName;
    private final String entityType;

    private boolean cancelled;

    public EntityAttackEvent(String playerName, String entityType) {
        this.playerName = playerName;
        this.entityType = entityType;
    }

    @ScriptMethod(description = "Gets the attcking player's name")
    public String getPlayerName() {
        return playerName;
    }

    @ScriptMethod(description = "Gets the target entity identifier")
    public String getEntityType() {
        return entityType;
    }

    @ScriptMethod(description = "Cancels the attack")
    public void cancel() {
        cancelled = true;
    }

    @ScriptMethod(description = "Checks wherther the attack was canceld")
    public boolean isCancelled() {
        return cancelled;
    }
}
