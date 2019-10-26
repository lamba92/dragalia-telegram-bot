plugins {
    kotlin("jvm") version "1.3.60-eap-76"
}

group = "com.github.lamba92"
version = "0.0.1"

repositories {
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://maven.pkg.github.com/")
    jcenter()
}

fun findProperty(propertyName: String): String? =
    project.findProperty(propertyName) as String? ?: System.getenv(propertyName)

repositories {
    jcenter()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://maven.pkg.github.com/${findProperty("githubAccount")}/${rootProject.name}") {
        name = "GitHubPackages"
        credentials {
            username = findProperty("githubAccount")
            password = findProperty("githubToken")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.telegram", "telegrambots", "4.4.0.1")
    implementation(lamba("dragalia-library-kodein-di-jvm", "1.0.0"))
    implementation(lamba("telegrambots-ktx", "0.0.1"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

@Suppress("unused")
fun DependencyHandler.lamba(module: String, version: String? = null): Any =
    "com.github.lamba92:$module${version?.let { ":$version" } ?: ""}"