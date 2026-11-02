// Module: NeoForge
plugins {
    id("com.gradleup.shadow")
}

val neoforgeVersion: String by extra
val architecturyApiVersion: String by extra

architectury {
    platformSetupLoomIde()
    neoForge()
}

configurations {
    val common by configurations.creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    val shadowBundle by configurations.creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    val bundledLibraries by configurations.creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    named("compileClasspath") {
        extendsFrom(common, bundledLibraries)
    }
    named("runtimeClasspath") {
        extendsFrom(common, bundledLibraries)
    }
    named("developmentNeoForge") {
        extendsFrom(common)
    }
}

repositories {
    maven(url = uri("https://maven.neoforged.net/releases/"))
}

val graalvmVersion: String by extra


dependencies {
    "neoForge"("net.neoforged:neoforge:${neoforgeVersion}")
    implementation("dev.architectury:architectury-neoforge:${architecturyApiVersion}")

    "common"(project(":common")) {
        isTransitive = false
    }

    "shadowBundle"(project(path = ":common", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }

    "bundledLibraries"("org.graalvm.js:js:${graalvmVersion}")
    "bundledLibraries"("org.graalvm.polyglot:polyglot:${graalvmVersion}")
    "bundledLibraries"("org.graalvm.sdk:nativebridge:${graalvmVersion}")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.shadowJar {
    configurations = listOf(
        project.configurations.getByName("shadowBundle"),
        project.configurations.getByName("bundledLibraries")
    )
    archiveClassifier.set("")
}

tasks.sourcesJar {
    from(project(":common").sourceSets.main.get().allSource)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}