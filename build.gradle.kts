plugins {
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"

    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
}

group = "cat.ohmushi"
version = "0.0.1-SNAPSHOT"

data class Versions(
    val jda: String = "6.3.0",
    val kord: String = "0.17.0",
    val jdaKtx: String = "0.14.1",
    val kotlinLogging: String = "7.0.3",
    val easyDiscordComponent: String = "1.0.1",
    val assertj: String = "3.27.6",
)

val versions = Versions()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile> {
    options.release.set(24)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("tools.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.assertj:assertj-core:${versions.assertj}")

    implementation("dev.kord:kord-core-jvm:${versions.kord}")
    implementation("net.dv8tion:JDA:${versions.jda}") {
        exclude(group = "club.minnced", module = "opus-java")
        exclude(group = "com.google.crypto.tink", module = "tink")
    }
    implementation("club.minnced:jda-ktx:${versions.jdaKtx}")
    implementation("io.github.oshai:kotlin-logging-jvm:${versions.kotlinLogging}")
    implementation("fr.ftnl.tools:EasyDiscordComponentV2-core:${versions.easyDiscordComponent}")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property",
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    buildInfo()
}
