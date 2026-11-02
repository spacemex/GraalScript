package com.github.spacemex.graalscript.typescript;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class TypeScriptCompiler implements AutoCloseable {

    public static final String ECMA_VERSION = "ES2025";
    public static final String MODULE_TYPE = "ESNext";

    private final Context context;
    private final Value typescript;

    public TypeScriptCompiler() throws IOException {
        context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.NONE)
                .allowHostClassLookup(name -> false)
                .allowIO(IOAccess.NONE)
                .allowCreateThread(false)
                .allowCreateProcess(false)
                .allowNativeAccess(false)
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        try (InputStream stream = TypeScriptCompiler.class.getResourceAsStream("/graalscript/compiler/typescript.js")) {
            if (stream == null) {
                throw new IOException("Embedded Typescript compiler not found");
            }

            context.eval("js", "var module = { exports: {} }; " + "var exports = module.exports;");

            String source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            context.eval(Source.newBuilder("js", source, "typescript.js").build());

            Value globalTs = context.getBindings("js").getMember("ts");
            Value moduleTs = context.getBindings("js").getMember("module").getMember("exports");

            if (globalTs != null && globalTs.hasMember("transpileModule")) {
                typescript = globalTs;
            } else if (moduleTs != null && moduleTs.hasMember("transpileModule")) {
                typescript = moduleTs;
            } else {
                throw new IllegalStateException("TypeScript compiler API unavailable");
            }
        } catch (IOException | RuntimeException e) {
            context.close();
            throw e;
        }
    }

    public CompilationResult compile(String fileName, String source) {
        Value options = context.eval("js", """
                ({
                    reportDiagnostics: true,
                    compilerOptions: {
                        isolatedModules: true
                    }
                })
                """);
        options.putMember("fileName", fileName);

        Value compilerOptions = options.getMember("compilerOptions");

        compilerOptions.putMember("target", typescript.getMember("ScriptTarget").getMember(ECMA_VERSION).asInt());
        compilerOptions.putMember("module", typescript.getMember("ModuleKind").getMember(MODULE_TYPE).asInt());

        Value result = typescript.getMember("transpileModule").execute(source, options);

        String javaScript = result.getMember("outputText").asString();
        List<Diagnostic> messages = new ArrayList<>();

        Value diagnostics = result.getMember("diagnostics");

        if (diagnostics != null && diagnostics.hasArrayElements()) {
            for (long i = 0; i < diagnostics.getArraySize(); i++) {
                Value diagnostic = diagnostics.getArrayElement(i);
                int code = diagnostic.getMember("code").asInt();
                boolean error = diagnostic.getMember("category").asInt() == typescript.getMember("DiagnosticCategory").getMember("Error").asInt();
                String message = typescript.getMember("flattenDiagnosticMessageText").execute(diagnostic.getMember("messageText"), "\n").asString();
                int line = 1;
                int column = 1;
                Value start = diagnostic.getMember("start");

                if (start != null && start.isNumber()) {
                    int offset = Math.min(start.asInt(), source.length());

                    for (int j = 0; j < offset; j++) {
                        if (source.charAt(j) == '\n') {
                            line++;
                            column = 1;
                        } else {
                            column++;
                        }
                    }
                }

                messages.add(new Diagnostic(code, message, line, column, error));
            }
        }

        return new CompilationResult(javaScript, messages);
    }

    @Override
    public void close() throws Exception {
        context.close();
    }

    public record Diagnostic(int code, String message, int line, int column, boolean error) {}
    public record CompilationResult(String javaScript, List<Diagnostic> diagnostics) {

        public boolean hasErrors() {
            return diagnostics.stream().anyMatch(Diagnostic::error);
        }
    }
}
