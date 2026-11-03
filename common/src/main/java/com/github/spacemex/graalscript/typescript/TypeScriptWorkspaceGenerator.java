package com.github.spacemex.graalscript.typescript;

import com.github.spacemex.graalscript.runtime.ScriptScope;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class TypeScriptWorkspaceGenerator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private TypeScriptWorkspaceGenerator() {}

    public static void generate(Path root) throws IOException {
        Files.createDirectories(root.resolve("generated"));

        for (ScriptScope scope : ScriptScope.values()) {
            Path directory = root.resolve(scope.getDirectory());

            Files.createDirectories(directory);

            Path output = directory.resolve("tsconfig.json");

            if (Files.exists(output)) {
                continue;
            }

            JsonObject config = new JsonObject();
            JsonObject options = new JsonObject();

            options.addProperty("target", TypeScriptCompiler.ECMA_VERSION);
            options.addProperty("module", TypeScriptCompiler.MODULE_TYPE);
            options.addProperty("moduleResolution", "bundler");
            options.addProperty("strict", true);
            options.addProperty("noEmit", true);
            options.addProperty("isolatedModules", true);

            JsonArray types = new JsonArray();
            options.add("types", types);

            JsonArray libs = new JsonArray();
            libs.add(TypeScriptCompiler.ECMA_VERSION);
            options.add("lib", libs);

            config.add("compilerOptions", options);

            JsonArray include = new JsonArray();

            include.add("./**/*.ts");
            include.add("../generated/" + scope.getDirectory() + ".d.ts");

            config.add("include", include);

            Files.writeString(output, GSON.toJson(config), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        }
    }
}
