plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.allopen") version "2.3.21"
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:quarkus-google-cloud-services-bom:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkiverse.microprofile:quarkus-microprofile:3.4.1")
    implementation("io.quarkiverse.googlecloudservices:quarkus-google-cloud-secret-manager")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    testImplementation("io.quarkus:quarkus-junit")
    testImplementation("io.rest-assured:rest-assured")
}

group = "no.roedt"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
        javaParameters = true
        /*
        Denne er for å unngå unødige advarsler om https://youtrack.jetbrains.com/issue/KT-73255
        Vi bruker egentlig bare konstruktør-varianten, men vil egentlig helst holde oss til kotlin sin standardvariant
        Så når dette er blitt standarden i kotlin - som det skal bli - så kan vi skru av denne
         */
        freeCompilerArgs = listOf("-Xannotation-default-target=param-property", "-Xjvm-default=all")
    }
}