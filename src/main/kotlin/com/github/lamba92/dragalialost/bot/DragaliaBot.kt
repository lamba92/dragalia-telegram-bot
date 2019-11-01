package com.github.lamba92.dragalialost.bot

import com.github.lamba92.dragalialost.kodeindi.dragaliaLostModule
import com.github.lamba92.telegrambots.extensions.TelegramPollingBotProvider
import com.github.lamba92.telegrambots.extensions.telegramBot
import io.ktor.client.features.logging.LogLevel
import kotlinx.coroutines.flow.toList

object DragaliaBot : TelegramPollingBotProvider {
    override val bot = telegramBot {
        botApiToken = System.getenv("DRAGALIA_TOKEN")
        botUsernameName = "DragaliaBot"

        kodein {
            import(dragaliaLostModule(true, LogLevel.BODY))
        }

        handlers {

            inlineQueries {

                handleQuery { inlineQuery ->
                    if (inlineQuery.query.length >= 2)
                        searchAllUseCase(inlineQuery.query)
                            .mapArticle()
                            .toList()
                            .let { respond(it) }
                }
            }
        }
    }

}