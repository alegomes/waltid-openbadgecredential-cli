/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.5/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.jvm)
    // id("org.jetbrains.kotlin.jvm") version "1.3.72"

    // Apply the application plugin to add support for building a CLI application in Java.
    application

    // id ("org.jetbrains.kotlin.kapt") version "1.4.30"

}

repositories {
    
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    // Required by https://github.com/multiformats/java-multibase
    // which is required by id.walt.crypto:waltid-crypto-jvm:1.0.1
    maven("https://jitpack.io")
    
    // Required by id.walt.credentials:waltid-verifiable-credentials and dependencies
    maven("https://maven.walt.id/repository/waltid/") 
    maven("https://maven.walt.id/repository/waltid-ssi-kit/")
    
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)

    // PicoCLI
    implementation ("info.picocli:picocli:4.6.1")

    // Walt.id

    // // I didn't use v1.1.1 because it depends on an artifact version 
    // // that doesn't exist at https://maven.walt.id 
    // // It sets waltid-sdjwt-1.SNAPSHOT instead of (waltid-sdjwt-1.0.0-SNAPSHOT)
    implementation("id.walt.credentials:waltid-verifiable-credentials:1.0.1")
    implementation("id.walt.crypto:waltid-crypto:1.1.1")

    // Kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    implementation("app.softwork:kotlinx-uuid-core:0.0.22")

//    val coroutines_version = "1.6.4"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutines_version")
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines_version")
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutines_version")
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutines_version")

// // Have no idea why, but this is needed for debugging in VSCode
    // implementation("org.jetbrains:markdown:0.5.0")
    // // implementation("app.softwork:kotlinx-uuid-core:LATEST") // java.lang.NoClassDefFoundError: kotlinx/uuid/UUID
    // implementation("app.softwork:kotlinx-uuid-core:0.0.22")
    // implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0") // java.lang.ClassNotFoundException: kotlinx.datetime.Clock$System


}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("waltid.openbadgecredential.cli.WaltCmdKt")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
