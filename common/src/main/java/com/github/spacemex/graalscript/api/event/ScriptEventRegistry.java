
package com.github.spacemex.graalscript.api.event;

import com.github.spacemex.graalscript.runtime.ScriptScope;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ScriptEventRegistry {

    private final Map<String, ScriptEvent<?>> events = new LinkedHashMap<>();
    private boolean sealed;

    public synchronized <T> ScriptEvent<T> register(String id, ScriptScope scope, Class<T> type) {
        if (sealed) {
            throw new IllegalStateException("Event registration has already finished");
        }

        Objects.requireNonNull(id);
        Objects.requireNonNull(scope);
        Objects.requireNonNull(type);

        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("Invalid event identifier: " + id);
        }

        if (events.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate script event: " + id);
        }

        ScriptEvent<T> event = new ScriptEvent<>(id, scope, type);

        events.put(id, event);

        return event;
    }

    public synchronized void seal() {
        sealed = true;
    }

    public synchronized void clearScope(ScriptScope scope) {
        for (ScriptEvent<?> event : events.values()) {
            if (event.scope() == scope) {
                event.clear();
            }
        }
    }

    public synchronized void setDispatcher(
            ScriptScope scope,
            ScriptEventDispatcher dispatcher
    ) {
        Objects.requireNonNull(scope);
        Objects.requireNonNull(dispatcher);

        for (ScriptEvent<?> event : events.values()) {
            if (event.scope() == scope) {
                event.setDispatcher(dispatcher);
            }
        }
    }
}
