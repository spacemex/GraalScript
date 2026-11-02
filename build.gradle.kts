// Module: Root
plugins {
    id("dev.architectury.loom-no-remap") version "1.17-SNAPSHOT" apply false
    id("architectury-plugin") version "3.5-SNAPSHOT"
    id("com.gradleup.shadow") version "9.3.2" apply false
}

allprojects {
    extra["modVersion"] = "1.0-ALPHA"
    extra["minecraftVersion"] = "26.1"
    extra["fabricApiVersion"] = "0.144.1"
    extra["fabricLoaderVersion"] = "0.18.5"
    extra["neoforgeVersion"] = "26.1.0.1-beta"
    extra["architecturyApiVersion"] = "20.0.2"
    extra["enabledPlatforms"] = "fabric,neoforge"
    extra["typeScriptVersion"] = "6.0.3"

    extra["graalvmVersion"] = "25.4.4.1.1"

    group = "com.github.spacemex"
    version = extra["modVersion"] as String
}

architectury {
    minecraft = "26.1"
}

val minecraftVersion: String by extra
val modVersion: String by extra
val typeScriptVersion: String by extra

subprojects {
    apply { plugin("dev.architectury.loom-no-remap") }
    apply { plugin("architectury-plugin") }
    apply { plugin("maven-publish") }

    val typeScriptCompiler by configurations.creating {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
    }

    dependencies {
        "minecraft"("net.minecraft:minecraft:${minecraftVersion}")
        add(typeScriptCompiler.name, "org.webjars.npm:typescript:${typeScriptVersion}")
    }

    extensions.configure<BasePluginExtension> {
        archivesName.set("GraalScript-${modVersion}+${minecraftVersion}-${project.name}")
    }

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()

        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(25)
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<AbstractArchiveTask> {
        archiveVersion.set("")
    }

    tasks.withType<ProcessResources> {
        val webjarRoot = "META-INF/resources/webjars/typescript/$typeScriptVersion"
        from({ zipTree(typeScriptCompiler.singleFile) }) {
            include("$webjarRoot/lib/typescript.js", "$webjarRoot/LICENSE.txt")

            eachFile {
                path = if (name == "typescript.js") {
                    "graalscript/compiler/typescript.js"
                } else {
                    "META-INF/licenses/typescript/LICENSE/txt"
                }
            }
            includeEmptyDirs = false
        }
    }
}
