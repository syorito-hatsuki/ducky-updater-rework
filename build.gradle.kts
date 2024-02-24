import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("fabric-loom")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}

val javaVersion = JavaVersion.VERSION_17
val loaderVersion: String by project
val minecraftVersion: String by project

val modVersion: String by project
version = "${DateTimeFormatter.ofPattern("yyyy.M").format(LocalDateTime.now())}.$modVersion-$minecraftVersion"

val mavenGroup: String by project
group = mavenGroup

repositories {
    maven("https://api.modrinth.com/maven")
}

dependencies {
    minecraft("com.mojang", "minecraft", minecraftVersion)

    val yarnMappings: String by project
    mappings("net.fabricmc", "yarn", yarnMappings, null, "v2")

    modImplementation("net.fabricmc", "fabric-loader", loaderVersion)

    val fabricKotlinVersion: String by project
    modImplementation("net.fabricmc", "fabric-language-kotlin", fabricKotlinVersion)

    val fabricVersion: String by project
    modImplementation("net.fabricmc.fabric-api", "fabric-api", fabricVersion)

    include(modImplementation("maven.modrinth", "modmenu-badges-lib", "hF72vnib"))

    val ktorVersion: String by project
    include(implementation("io.ktor", "ktor-client-core", ktorVersion))
    include(implementation("io.ktor", "ktor-client-cio-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-client-cio", ktorVersion))
    include(implementation("io.ktor", "ktor-client-content-negotiation", ktorVersion))
    include(implementation("io.ktor", "ktor-serialization-kotlinx-json", ktorVersion))


    implementation("org.xerial", "sqlite-jdbc", "3.44.1.0")
    implementation("com.zaxxer", "HikariCP", "5.1.0")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }

    jar {
        from("LICENSE")
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "version" to project.version, "javaVersion" to javaVersion.toString()
                )
            )
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }

    test {
        useJUnitPlatform()
    }
}