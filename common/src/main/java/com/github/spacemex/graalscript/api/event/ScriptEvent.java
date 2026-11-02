package com.github.spacemex.graalscript.api.event;

import com.github.spacemex.graalscript.runtime.ScriptScope;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ScriptEvent<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger("GraalScript");
    private final String id;
    private final ScriptScope scope;
    private final Class<T> type;

    private final Map<Long, Listener> listeners = new LinkedHashMap<>();
    private Thread ownerThread;
    private ScriptEventDispatcher dispatcher;
    private long nextListenerId = 1;
    private long generation = 0;

    private record Listener(Value callback, ScriptSubscription subscription) {
    }

    ScriptEvent(String id, ScriptScope scope, Class<T> type) {
        this.id = id;
        this.scope = scope;
        this.type = type;
    }

    public String id() {
        return id;
    }

    public ScriptScope scope() {
        return scope;
    }

    public Class<T> payloadType() {
        return type;
    }

    public synchronized ScriptSubscription listen(Value callback) {
        Objects.requireNonNull(callback, "callback");

        if (!callback.canExecute()) {
            throw new IllegalArgumentException(
                    "Event " + id + " requires a JavaScript function"
            );
        }

        if (dispatcher != null && !dispatcher.isEventThread()) {
            throw new IllegalStateException(
                    "Cannot register listener for " + id +
                            " outside its event thread"
            );
        }

        Thread current = Thread.currentThread();

        if (ownerThread == null) {
            ownerThread = current;
        } else if (ownerThread != current) {
            throw new IllegalStateException(
                    "Event " + id + " belongs to another script thread"
            );
        }

        long listenerId = nextListenerId++;

        ScriptSubscription subscription =
                new ScriptSubscription(() -> remove(listenerId));

        listeners.put(
                listenerId,
                new Listener(callback, subscription)
        );

        return subscription;
    }

    /**
     * Fires a notification event.
     * If a dispatcher exists, off-thread calls are queued.
     * <p>
     * Do not use this method when Minecraft needs a
     * synchronous cancellation result.
     */
    public void fire(T payload) {
        Objects.requireNonNull(payload, "payload");
        type.cast(payload);

        ScriptEventDispatcher target;
        Thread expectedThread;
        long expectedGeneration;

        synchronized (this) {
            if (listeners.isEmpty()) {
                return;
            }

            target = dispatcher;
            expectedThread = ownerThread;
            expectedGeneration = generation;
        }

        if (target != null && !target.isEventThread()) {
            try {
                target.execute(
                        () -> dispatchNow(payload, expectedGeneration)
                );
            } catch (RuntimeException exception) {
                LOGGER.error(
                        "Failed to schedule event {}",
                        id,
                        exception
                );
            }
            return;
        }

        if (Thread.currentThread() != expectedThread) {
            LOGGER.error(
                    "Event {} fired from an invalid thread",
                    id
            );
            return;
        }

        dispatchNow(payload, expectedGeneration);
    }

    /**
     * Fires an event immediately on its script thread.
     * Use for cancellable events where the caller needs
     * the mutated payload before returning to Minecraft.
     */
    public void fireSync(T payload) {
        Objects.requireNonNull(payload, "payload");
        type.cast(payload);

        long expectedGeneration;

        synchronized (this) {
            if (listeners.isEmpty()) {
                return;
            }

            if (Thread.currentThread() != ownerThread ||
                    (dispatcher != null && !dispatcher.isEventThread())) {
                throw new IllegalStateException(
                        "Synchronous event " + id +
                                " must execute on its script thread"
                );
            }

            expectedGeneration = generation;
        }

        dispatchNow(payload, expectedGeneration);
    }

    private void dispatchNow(T payload, long expectedGeneration) {
        List<Listener> snapshot;

        synchronized (this) {
            // An old scheduled event must not execute after
            // the server scripting context has been restarted.
            if (generation != expectedGeneration) {
                return;
            }

            if (Thread.currentThread() != ownerThread ||
                    (dispatcher != null && !dispatcher.isEventThread())) {
                LOGGER.error(
                        "Event {} was dispatched on an invalid thread",
                        id
                );
                return;
            }

            snapshot = List.copyOf(listeners.values());
        }

        for (Listener listener : snapshot) {
            if (!listener.subscription().isActive()) {
                continue;
            }

            try {
                listener.callback().execute((Object) payload);
            } catch (RuntimeException exception) {
                LOGGER.error(
                        "Script callback failed for event {}",
                        id,
                        exception
                );
            }
        }
    }

    private synchronized void remove(long listenerId) {
        Listener removed = listeners.remove(listenerId);

        if (removed != null) {
            removed.subscription().invalidate();
        }
    }

    synchronized void setDispatcher(
            ScriptEventDispatcher dispatcher
    ) {
        this.dispatcher = dispatcher;
    }

    public synchronized void clear() {
        // Invalidate subscriptions before closing the
        // GraalVM context that owns their callbacks.
        for (Listener listener : listeners.values()) {
            listener.subscription().invalidate();
        }

        listeners.clear();
        ownerThread = null;
        dispatcher = null;

        // Discards tasks queued by a previous runtime.
        generation++;
    }


}
