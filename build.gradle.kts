plugins {
    kotlin("jvm") version "2.3.0-Beta2"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "org.bysenom"
version = "1.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// Deploy zur lokalen Test-Server-Plugins-Mappe
val deployTarget = file("C:/Users/SashaW/IdeaProjects/HubPlugin/run/plugins")

tasks.register<Copy>("deployPlugin") {
    dependsOn(tasks.named("shadowJar"))
    from(layout.buildDirectory.dir("libs"))
    include("SurvivalPlus-*-all.jar")
    into(deployTarget)
    doFirst {
        // Alte Versionen entfernen, damit nur 1 Datei liegt
        if (deployTarget.exists()) {
            project.delete(project.fileTree(deployTarget) { include("SurvivalPlus-*-all.jar") })
        } else {
            deployTarget.mkdirs()
        }
    }
    doLast {
        println("✓ Plugin nach ${deployTarget} kopiert.")
    }
}

// Nach dem normalen Build automatisch deployen
tasks.build {
    finalizedBy("deployPlugin")
}
