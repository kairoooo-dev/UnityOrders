import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.5"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

group = properties["group"] as String
version = properties["version"] as String
description = properties["description"] as String

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(properties["javaVersion"] as String))
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        name = "papi"
    }
    maven("https://repo.essentialsx.net/releases/") {
        name = "essentials"
    }
    maven("https://repo.lucko.me/") {
        name = "lucko"
    }
}

dependencies {
    // --- Paper API (compile) ---
    paperweight.paperDevBundle(properties["paperApiVersion"] as String)

    // --- Vault (provided) ---
    compileOnly("com.github.MilkBowl:VaultAPI:${properties["vaultVersion"]}") {
        exclude(group = "org.bukkit")
    }

    // --- PlaceholderAPI (provided) ---
    compileOnly("me.clip:placeholderapi:${properties["placeholderapiVersion"]}")

    // --- LuckPerms (provided) ---
    compileOnly("net.luckperms:api:${properties["luckpermsVersion"]}")

    // --- Adventure Platform (bundled) ---
    implementation("net.kyori:adventure-platform-bukkit:${properties["adventurePlatformVersion"]}")

    // --- HikariCP (bundled) ---
    implementation("com.zaxxer:HikariCP:${properties["hikariVersion"]}")
    implementation("org.slf4j:slf4j-api:${properties["slf4jVersion"]}")
    implementation("org.slf4j:slf4j-jdk14:${properties["slf4jVersion"]}")

    // --- SQLite JDBC (bundled) ---
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")

    // --- MySQL / MariaDB (provided by server or bundled) ---
    implementation("com.mysql:mysql-connector-j:9.1.0")

    // --- PostgreSQL (bundled) ---
    implementation("org.postgresql:postgresql:42.7.4")

    // --- MongoDB (bundled) ---
    implementation("org.mongodb:mongodb-driver-sync:5.2.0")

    // --- Tests ---
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.mockito:mockito-core:5.14.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.0")
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Xlint:-processing"))
    }

    withType<ShadowJar> {
        archiveBaseName.set("UnityOrders")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())

        // Relocate bundled dependencies to avoid conflicts
        relocate("com.zaxxer.hikari", "com.unity.libs.hikari")
        relocate("org.slf4j", "com.unity.libs.slf4j")
        relocate("net.kyori.adventure.platform", "com.unity.libs.adventure.platform")
        relocate("org.xerial.sqlite", "com.unity.libs.sqlite")
        relocate("com.mysql", "com.unity.libs.mysql")
        relocate("org.postgresql", "com.unity.libs.postgresql")
        relocate("com.mongodb", "com.unity.libs.mongodb")
        relocate("org.bson", "com.unity.libs.bson")

        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
            "version" to project.version,
            "description" to project.description
        )
        inputs.properties(props)
        filesMatching(listOf("plugin.yml", "config.yml", "messages.yml")) {
            expand(props)
        }
    }
}
