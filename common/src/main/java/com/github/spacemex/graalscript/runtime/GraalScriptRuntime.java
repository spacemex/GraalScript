package com.github.spacemex.graalscript.runtime;

import com.github.spacemex.graalscript.api.ScriptApiRegistry;
import com.github.spacemex.graalscript.api.annotation.ScriptMethod;
import com.github.spacemex.graalscript.api.event.ScriptEventRegistry;
import com.github.spacemex.graalscript.typescript.TypeDefinitionGenerator;
import com.github.spacemex.graalscript.typescript.TypeScriptCompiler;
import com.github.spacemex.graalscript.typescript.TypeScriptWorkspaceGenerator;
import dev.architectury.platform.Platform;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class GraalScriptRuntime {

    private static final Logger LOGGER = LoggerFactory.getLogger("GraalScript");
    private final ScriptApiRegistry registry;
    private TypeScriptCompiler compiler;
    private final ScriptEventRegistry events;
    private final Map<ScriptScope, Context> contexts = new EnumMap<>(ScriptScope.class);

    public GraalScriptRuntime(ScriptApiRegistry registry, ScriptEventRegistry events) {
        this.registry = registry;
        this.events = events;
    }

    public synchronized void start(ScriptScope scope) {
        if (contexts.containsKey(scope)) {
            return;
        }

        Path root = Platform.getConfigFolder().resolve("graalscript");
        Path directory = root.resolve(scope.getDirectory());

        Context context = null;

        try {
            TypeScriptWorkspaceGenerator.generate(root);

            for (ScriptScope declarationScope : ScriptScope.values()) {
                TypeDefinitionGenerator.generate(
                        root,
                        declarationScope,
                        registry.forScope(declarationScope),
                        events
                );
            }

            Map<String, Object> bindings = registry.forScope(scope);

            HostAccess hostAccess = HostAccess
                    .newBuilder(HostAccess.EXPLICIT)
                    .allowAccessAnnotatedBy(ScriptMethod.class)
                    .build();

            context = Context.newBuilder("js")
                    .allowHostAccess(hostAccess)
                    .allowHostClassLookup(name -> false)
                    .allowHostClassLoading(false)
                    .allowIO(IOAccess.NONE)
                    .allowCreateThread(false)
                    .allowCreateProcess(false)
                    .allowNativeAccess(false)
                    .option("engine.WarnInterpreterOnly", "false")
                    .build();

            Value globals = context.getBindings("js");

            bindings.forEach(globals::putMember);

            List<Path> scripts;

            try (Stream<Path> files = Files.walk(directory)) {
                scripts = files.filter(Files::isRegularFile).filter(path -> {
                    String name = path.getFileName().toString();
                    return name.endsWith(".js") || (name.endsWith(".ts") && !name.endsWith(".d.ts"));
                }).sorted().toList();
            }

            int executed = 0;

            for (Path script : scripts) {
                try {
                    String source = Files.readString(script, StandardCharsets.UTF_8);
                    String fileName = directory.relativize(script).toString().replace('\\', '/');
                    String javascript = source;

                    if (fileName.endsWith(".ts")) {
                        if (compiler == null) {
                            compiler = new TypeScriptCompiler();
                        }

                        TypeScriptCompiler.CompilationResult result = compiler.compile(fileName, source);

                        for (TypeScriptCompiler.Diagnostic diagnostic : result.diagnostics()) {
                            String message = String.format("%s:%d:%d - TS%d: %s", fileName, diagnostic.line(),
                                    diagnostic.column(), diagnostic.code(), diagnostic.message());

                            if (diagnostic.error()) {
                                LOGGER.error(message);
                            } else {
                                LOGGER.warn(message);
                            }
                        }

                        if (result.hasErrors()) {
                            LOGGER.error("Skipping script due to compilation errors: {}", fileName);
                            continue;
                        }

                        javascript = result.javaScript();
                    }

                    Source jsSource = Source.newBuilder("js", javascript, fileName).build();

                    context.eval(jsSource);

                    executed++;

                    LOGGER.debug("Executed {} script: {}", scope, fileName);
                } catch (Exception exception) {
                    LOGGER.error("Failed to execute script: {}", script, exception);
                }
            }

            contexts.put(scope, context);
            LOGGER.debug("Initialized {} runtime ({} scripts executed)", scope, executed);
        } catch (Exception exception) {
            if (context != null) {
                context.close();
            }
            LOGGER.error("Failed to initialize {} runtime", scope, exception);
        }
    }


    public synchronized void stop(ScriptScope scope) {
        Context context = contexts.remove(scope);

        if (context != null) {
            context.close();
            LOGGER.info("Stopped {} runtime", scope);
        }
    }
}