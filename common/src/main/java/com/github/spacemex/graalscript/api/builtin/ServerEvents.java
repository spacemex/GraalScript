package com.github.spacemex.graalscript.api.builtin;

import com.github.spacemex.graalscript.api.annotation.ScriptBinding;
import com.github.spacemex.graalscript.api.annotation.ScriptCallback;
import com.github.spacemex.graalscript.api.annotation.ScriptMethod;
import com.github.spacemex.graalscript.api.builtin.event.EntityAttackEvent;
import com.github.spacemex.graalscript.api.builtin.event.PlayerJoinEvent;
import com.github.spacemex.graalscript.api.event.ScriptEvent;
import com.github.spacemex.graalscript.api.event.ScriptSubscription;
import com.github.spacemex.graalscript.runtime.ScriptScope;
import org.graalvm.polyglot.Value;

@ScriptBinding(name = "ServerEvents", scopes = {ScriptScope.SERVER})
public final class ServerEvents {

    private ScriptEvent<PlayerJoinEvent> playerJoinEvent;
    private ScriptEvent<EntityAttackEvent> attackEvent;

    public ServerEvents(ScriptEvent<PlayerJoinEvent> playerJoinEvent,
                        ScriptEvent<EntityAttackEvent> attackEvent) {
        this.playerJoinEvent = playerJoinEvent;
        this.attackEvent = attackEvent;
    }

    @ScriptMethod(description = "Runs when a player joinds the server.")
    @ScriptCallback(PlayerJoinEvent.class)
    public ScriptSubscription playerJoin(Value callback) {
        return playerJoinEvent.listen(callback);
    }

    @ScriptMethod(description = "Runs before a player attack an entity")
    @ScriptCallback(EntityAttackEvent.class)
    public ScriptSubscription attackEntity(Value callback) {
        return attackEvent.listen(callback);
    }
}
