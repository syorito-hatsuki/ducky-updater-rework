import com.modrinth.minotaur.TaskModrinthUpload
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val javaVersion = JavaVersion.VERSION_21
val loaderVersion: String by project
val minecraftVersion: String by project
val modVersion: String by project
val mavenGroup: String by project
val fabricKotlinVersion: String by project
val fabricVersion: String by project

plugins {
    id("fabric-loom")
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.modrinth.minotaur")
}

base {
    val archivesBaseName: String by project
    archivesName.set("$archivesBaseName-$modVersion-$minecraftVersion")
}

repositories {
    maven("https://api.modrinth.com/maven")
}

dependencies {
    minecraft("com.mojang", "minecraft", minecraftVersion)

    val yarnMappings: String by project
    mappings("net.fabricmc", "yarn", yarnMappings, null, "v2")

    modImplementation("net.fabricmc", "fabric-loader", loaderVersion)

    modImplementation("net.fabricmc", "fabric-language-kotlin", fabricKotlinVersion)

    modImplementation("net.fabricmc.fabric-api", "fabric-api", fabricVersion)

    include(modImplementation("maven.modrinth", "modmenu-badges-lib", "2026.2.1"))
    include(modImplementation("maven.modrinth", "fstats", "QNO1tRop"))

    val ktorVersion: String by project
    include(implementation("io.ktor", "ktor-client-cio-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-client-content-negotiation-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-client-core-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-events-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-http-cio-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-http-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-io-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-network-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-network-tls-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-serialization-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-serialization-kotlinx-json-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-serialization-kotlinx-jvm", ktorVersion))
    include(implementation("io.ktor", "ktor-utils-jvm", ktorVersion))

    include(implementation("org.xerial", "sqlite-jdbc", "3.51.1.0"))
    include(implementation("com.zaxxer", "HikariCP", "7.0.2"))

    include(implementation("net.lingala.zip4j", "zip4j", "2.11.5"))
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("ducky-updater-rework")
    versionName.set("Ducky Updater: ReWork $modVersion")
    versionNumber.set(modVersion)
    versionType.set("release")
    uploadFile.set(tasks.remapJar)
    additionalFiles.add(tasks.remapSourcesJar)
    gameVersions.addAll("1.21.11")
    loaders.add("fabric")
    changelog.set(rootProject.file("CHANGELOG.md").readText())
    dependencies {
        required.project("fabric-api", "fabric-language-kotlin")
        embedded.project("fstats", "modmenu-badges-lib")
    }
}

tasks {

    named("modrinth").configure {
        @Suppress("UnstableApiUsage") doLast {
            (this@configure as TaskModrinthUpload).uploadInfo?.let {
                "https://modrinth.com/mod/ducky-updater-rework/version/${it.id}".apply {
                    println(this)
                    rootProject.file("build/modrinth_url.txt").writeText(this)
                }
            } ?: return@doLast
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }

    jar {
        from("LICENSE")
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to modVersion))
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
