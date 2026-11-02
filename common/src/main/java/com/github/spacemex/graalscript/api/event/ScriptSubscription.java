package com.github.spacemex.graalscript.api.event;

import com.github.spacemex.graalscript.api.annotation.ScriptMethod;
import com.github.spacemex.graalscript.api.annotation.ScriptType;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@ScriptType(name = "EventSubscription")
public final class ScriptSubscription {

    private final Runnable remover;
    private final AtomicBoolean active = new AtomicBoolean(true);

    ScriptSubscription(Runnable remover) {
        this.remover = Objects.requireNonNull(remover);
    }

    @ScriptMethod(description = "Unsubscribes from the event.")
    public boolean unsubscribe() {
        if (!active.compareAndSet(true, false)) {
            return false;
        }

        remover.run();
        return true;
    }

    @ScriptMethod(description = "Checks whether the subscription is active")
    public boolean isActive() {
        return active.get();
    }

    void invalidate() {
        active.set(false);
    }
}
