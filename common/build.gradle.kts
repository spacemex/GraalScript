// Module: Common
val enabledPlatforms: String by extra
architectury {
    common(enabledPlatforms.split(","))
}

val fabricLoaderVersion: String by extra
val architecturyApiVersion: String by extra
val graalvmVersion: String by extra

dependencies {
    implementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    implementation("dev.architectury:architectury:${architecturyApiVersion}")

    compileOnly("org.graalvm.js:js-language:${graalvmVersion}")
    compileOnly("org.graalvm.polyglot:polyglot:${graalvmVersion}")
}