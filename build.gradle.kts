import com.modrinth.minotaur.TaskModrinthUpload
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val archivesBaseName = project.property("archivesBaseName") as String
val mavenGroup = project.property("mavenGroup") as String
val modVersion = project.property("modVersion") as String

val javaVersion = JavaVersion.VERSION_25

plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.minotaur)
}

base {
    archivesName.set(archivesBaseName)
}

group = mavenGroup
version = modVersion

repositories {
    maven("https://api.modrinth.com/maven")
    maven {
        name = "faststatsReleases"
        url = uri("https://repo.faststats.dev/releases")
    }    
    maven {
        url = uri("https://repo.faststats.dev/snapshots")
    }
}

dependencies {
    minecraft(libs.minecraft)

    implementation(libs.fabric.api)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.language.kotlin)

    embed(libs.modmenu.badges)

    embed(libs.faststats)

    embed(libs.bundles.ktor)

    embed(libs.sqlite)
    embed(libs.hikari)

    embed(libs.zip4j)
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set(archivesBaseName)
    versionName.set("Ducky Updater: ReWork $modVersion")
    versionNumber.set(modVersion)
    versionType.set("release")
    uploadFile.set(tasks.jar)
    project.afterEvaluate {
        tasks.findByName("sourcesJar")?.let {
            additionalFiles.add(it)
        }
    }
    gameVersions.addAll("26.2")
    loaders.add("fabric")
    changelog.set(rootProject.file("CHANGELOG.md").readText())
    dependencies {
        required.project("fabric-api", "fabric-language-kotlin")
        embedded.project("modmenu-badges-lib")
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

tasks {
    jar {
        from("LICENSE")
    }

    named("modrinth").configure {
        @Suppress("UnstableApiUsage") doLast {
            (this@configure as TaskModrinthUpload).uploadInfo?.let {
                rootProject.file("build/modrinth_url.txt").writeText(
                    "https://modrinth.com/mod/$archivesBaseName/version/${it.id}".apply(::println)
                )
            } ?: return@doLast
        }
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "fabricLoader" to libs.fabric.loader.get().version,
                "minecraft" to libs.minecraft.get().version,
                "version" to modVersion
            ))
        }
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }
}

fun DependencyHandlerScope.embed(projectDependency: Provider<*>) {
    implementation(projectDependency)
    include(projectDependency)
}