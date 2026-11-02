package com.github.spacemex.graalscript.api.builtin.event;

import com.github.spacemex.graalscript.api.annotation.ScriptMethod;
import com.github.spacemex.graalscript.api.annotation.ScriptType;

@ScriptType(name = "PlayerJoinEvent")
public record PlayerJoinEvent(String playerName, String playerUuid) {

    @ScriptMethod(description = "Gets the player's name.")
    public String getPlayerName() {
        return playerName;
    }

    @ScriptMethod(description = "Gets the player's UUID.")
    public String getPlayerUuid() {
        return playerUuid;
    }
}
