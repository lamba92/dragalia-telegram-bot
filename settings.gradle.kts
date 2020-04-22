pluginManagement {
    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://plugins.gradle.org/m2/")
        jcenter()
        gradlePluginPortal()
    }
}
rootProject.name = "dragalia-telegram-bot"

if (file("../telegrambots-ktx").run { exists() && isDirectory })
    includeBuild("../telegrambots-ktx") {
        dependencySubstitution {
            substitute(module("com.github.lamba92:telegrambots-ktx")).with(project(":"))
        }
    }
