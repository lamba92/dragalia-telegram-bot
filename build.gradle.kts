import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.SessionHandler

plugins {
    kotlin("jvm") version "1.3.60-eap-76"
    id("org.hidetake.ssh") version "2.10.1"
    application
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
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://dl.bintray.com/kotlin/kotlinx.html")
    mavenCentral()
    jcenter()
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
    implementation(lamba("dragalia-library-kodein-di-jvm", "1.0.5"))
    implementation(lamba("telegrambots-ktx", "0.0.1"))
    implementation("com.vdurmont", "emoji-java", "5.1.1")
}

application {
    mainClassName = "com.github.lamba92.dragalialost.bot.MainKt"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

val raspiLocal = remotes.create("raspi-local") {
    val raspiPassword: String by project
    host = "192.168.1.101"
    user = "pi"
    password = raspiPassword
}

tasks.create("raspiLocalDeploy") {
    val installDist by tasks.named<Sync>("installDist")
    dependsOn(installDist)
    doLast {
        ssh.runSessions {
            session(raspiLocal) {
                executeSudo("systemctl stop dragalia-telegram-bot")
                put(installDist.destinationDir, "/home/pi/workspace")
                execute("chmod u+x /home/pi/workspace/dragalia-telegram-bot/bin/dragalia-telegram-bot")
                executeSudo("systemctl start dragalia-telegram-bot")
            }
        }
    }
}

@Suppress("unused")
fun DependencyHandler.lamba(module: String, version: String? = null): Any =
    "com.github.lamba92:$module${version?.let { ":$version" } ?: ""}"

fun Service.runSessions(action: RunHandler.() -> Unit) =
    run(delegateClosureOf(action))

fun RunHandler.session(vararg remotes: Remote, action: SessionHandler.() -> Unit) =
    session(*remotes, delegateClosureOf(action))

fun SessionHandler.put(from: Any, into: Any) =
    put(hashMapOf("from" to from, "into" to into))