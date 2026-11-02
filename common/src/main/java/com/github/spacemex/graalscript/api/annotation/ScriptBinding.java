package com.github.spacemex.graalscript.api.annotation;

import com.github.spacemex.graalscript.runtime.ScriptScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptBinding {

    String name();

    ScriptScope[] scopes() default {
            ScriptScope.STARTUP,
            ScriptScope.SERVER,
            ScriptScope.CLIENT,
    };
}
