package com.github.lamba92.dragalialost.bot

import com.github.lamba92.dragalialost.di.dragaliaLostModule
import com.github.lamba92.dragalialost.di.dragaliaMongoDBCacheModule
import com.github.lamba92.telegrambots.extensions.KApiContextInitializer
import com.github.lamba92.telegrambots.extensions.text

fun main(): Unit = KApiContextInitializer {

    registerPollingBot {

        botApiToken = System.getenv("BOT_TOKEN")
        botUsername = "DragaliaBot"

        kodein {

            import(dragaliaLostModule())
            import(
                dragaliaMongoDBCacheModule(
                    System.getenv("DB_HOST"),
                    System.getenv("DB_PORT")?.toInt() ?: 27017,
                    System.getenv("DB_NAME") ?: "db"
                )
            )

        }

        handlers {

            inlineQueries {
                respond(searchAllUseCase.buildAction(query.text).mapArticle())
            }

            messages {
                if (message.from.id == 140058014 && message.text == "/clearcache") {
                    respond {
                        text = "Deleting..."
                    }
                    gamepediaCache.invalidateCache()
                    respond {
                        text = "Done"
                    }
                }
            }
        }
    }

}

