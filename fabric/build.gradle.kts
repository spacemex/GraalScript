// Module: Fabric
plugins {
    id("com.gradleup.shadow")
}

val fabricLoaderVersion: String by extra
val fabricApiVersion: String by extra
val architecturyApiVersion: String by extra

architectury {
    platformSetupLoomIde()
    fabric()
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
    named("developmentFabric") {
        extendsFrom(common)
    }
}

val graalvmVersion: String by extra

dependencies {
    implementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    implementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    implementation("dev.architectury:architectury-fabric:${architecturyApiVersion}")

    "common"(project(":common")) {
        isTransitive = false
    }

    "shadowBundle"(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    "bundledLibraries"("org.graalvm.js:js:${graalvmVersion}")
    "bundledLibraries"("org.graalvm.polyglot:polyglot:${graalvmVersion}")
    "bundledLibraries"("org.graalvm.sdk:nativebridge:${graalvmVersion}")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
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