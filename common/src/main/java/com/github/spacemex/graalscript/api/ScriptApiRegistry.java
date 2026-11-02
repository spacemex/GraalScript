package com.github.spacemex.graalscript.api;

import com.github.spacemex.graalscript.api.annotation.ScriptBinding;
import com.github.spacemex.graalscript.api.annotation.ScriptMethod;
import com.github.spacemex.graalscript.runtime.ScriptScope;

import java.util.*;

public final class ScriptApiRegistry {

    private final Map<String, Object> bindings = new LinkedHashMap<>();
    private boolean sealed;

    public synchronized void register(Object instance) {
        if (sealed) {
            throw new IllegalStateException("Script API registrations has already finished");
        }

        Objects.requireNonNull(instance);

        Class<?> type = instance.getClass();
        ScriptBinding annotation = type.getAnnotation(ScriptBinding.class);

        if (annotation == null) {
            throw new IllegalArgumentException(type.getName() + " is missing @ScriptBinding");
        }

        if (!annotation.name().matches("[A-Za-z_$][A-Za-z0-9_$]*")) {
            throw new IllegalArgumentException("Invalid binding name: " + annotation.name());
        }

        boolean hasMethods = Arrays.stream(type.getMethods()).anyMatch(m -> m.isAnnotationPresent(ScriptMethod.class));

        if (!hasMethods) {
            throw new IllegalArgumentException(type.getName() + " has no @ScriptMethod methods");
        }

        if (bindings.containsKey(annotation.name())) {
            throw new IllegalArgumentException("Duplicate script binding: " + annotation.name());
        }

        bindings.put(annotation.name(), instance);
    }

    public synchronized Map<String, Object> forScope(ScriptScope scope) {
        Map<String, Object> result = new LinkedHashMap<>();

        bindings.forEach((name, inst) -> {
            ScriptBinding annotation = inst.getClass().getAnnotation(ScriptBinding.class);

            if (Arrays.asList(annotation.scopes()).contains(scope)) {
                result.put(name, inst);
            }
        });

        return Collections.unmodifiableMap(result);
    }

    public synchronized void seal() {
        sealed = true;
    }
}
