pluginManagement {
    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://plugins.gradle.org/m2/")
        jcenter()
        gradlePluginPortal()
    }
}

rootProject.name = "dragalia-telegram-bot"

if (file("../mongo-bootstrap").run { exists() && isDirectory })
    includeBuild("../mongo-bootstrap") {
        dependencySubstitution {
            substitute(module("com.github.lamba92:mongo-bootstrapper")).with(project(":"))
        }
    }

if (file("../telegrambots-ktx").run { exists() && isDirectory })
    includeBuild("../telegrambots-ktx") {
        dependencySubstitution {
            substitute(module("com.github.lamba92:telegrambots-ktx")).with(project(":"))
        }
    }

if (file("../dragalia-library").run { exists() && isDirectory })
    includeBuild("../dragalia-library") {
        dependencySubstitution {
            listOf("domain", "data", "core", "kodein-di").forEach {
                substitute(module("com.github.lamba92:dragalia-library-$it")).with(project(":$it"))
            }
        }
    }
