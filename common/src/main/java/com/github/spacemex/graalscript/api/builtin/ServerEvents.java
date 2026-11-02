package com.github.spacemex.graalscript.api.builtin;

import com.github.spacemex.graalscript.api.annotation.ScriptBinding;
import com.github.spacemex.graalscript.api.annotation.ScriptCallback;
import com.github.spacemex.graalscript.api.annotation.ScriptEventRouter;
import com.github.spacemex.graalscript.api.annotation.ScriptMethod;
import com.github.spacemex.graalscript.api.builtin.event.EntityAttackEvent;
import com.github.spacemex.graalscript.api.builtin.event.PlayerJoinEvent;
import com.github.spacemex.graalscript.api.event.ScriptEventRegistry;
import com.github.spacemex.graalscript.api.event.ScriptSubscription;
import com.github.spacemex.graalscript.runtime.ScriptScope;
import org.graalvm.polyglot.Value;

@ScriptBinding(name = "ServerEvents", scopes = {ScriptScope.SERVER})
public final class ServerEvents {

    private final ScriptEventRegistry registry;

    public ServerEvents(ScriptEventRegistry registry) {
        this.registry = registry;
    }

    @ScriptMethod(description = "Registers a listener for a server event.")
    @ScriptEventRouter
    public ScriptSubscription on(String id, Value callback) {
        return registry.listen(id, ScriptScope.SERVER, callback);
    }

    @ScriptMethod(description = "Runs when a player joins the server.")
    @ScriptCallback(PlayerJoinEvent.class)
    public ScriptSubscription playerJoin(Value callback) {
        return registry.listen(
                "graalscript:player_join",
                ScriptScope.SERVER,
                callback
        );
    }

    @ScriptMethod(description = "Runs before a player attacks an entity.")
    @ScriptCallback(EntityAttackEvent.class)
    public ScriptSubscription attackEntity(Value callback) {
        return registry.listen(
                "graalscript:attack_entity",
                ScriptScope.SERVER,
                callback
        );
    }
}
