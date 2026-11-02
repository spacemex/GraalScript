package com.github.spacemex.graalscript.api.builtin.event;

import com.github.spacemex.graalscript.api.annotation.ScriptMethod;
import com.github.spacemex.graalscript.api.annotation.ScriptType;
import net.minecraft.server.level.ServerPlayer;

@ScriptType(name = "PlayerJoinEvent")
public record PlayerJoinEvent(ServerPlayer player) {

    @ScriptMethod(description = "Gets the player's name.")
    public String getPlayerName() {
        return player.getPlainTextName();
    }

    @ScriptMethod(description = "Gets the player's UUID.")
    public String getPlayerUuid() {
        return player.getStringUUID();
    }
}
