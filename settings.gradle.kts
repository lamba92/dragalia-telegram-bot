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

//if (file("../dragalia-library").run { exists() && isDirectory })
//    includeBuild("../dragalia-library") {
//        dependencySubstitution {
//            substitute(module("com.github.lamba92:dragalia-library-kodein-di-jvm")).with(project(":kodein-di"))
//            substitute(module("com.github.lamba92:dragalia-library-core-jvm")).with(project(":core"))
//            substitute(module("com.github.lamba92:dragalia-library-data-jvm")).with(project(":data"))
//            substitute(module("com.github.lamba92:dragalia-library-domain-jvm")).with(project(":domain"))
//        }
//    }