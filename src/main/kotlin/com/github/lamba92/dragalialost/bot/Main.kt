package com.github.lamba92.dragalialost.bot

import com.github.lamba92.dragalialost.di.dragaliaLostModule
import com.github.lamba92.dragalialost.di.dragaliaMongoDBCacheModule
import com.github.lamba92.telegrambots.extensions.KApiContextInitializer
import com.github.lamba92.telegrambots.extensions.text
import com.github.lamba92.utils.mongodb.bootstrap.waitUntilMongoIsUp

suspend fun main() {

    waitUntilMongoIsUp(DB_HOST, DB_PORT)

    KApiContextInitializer {

        registerPollingBot {

            botApiToken = BOT_TOKEN
            botUsername = "DragaliaBot"

            kodein {
                import(dragaliaLostModule())
                import(dragaliaMongoDBCacheModule(DB_HOST, DB_PORT, DB_NAME))
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
}

