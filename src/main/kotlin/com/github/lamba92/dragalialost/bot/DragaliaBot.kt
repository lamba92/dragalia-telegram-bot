package com.github.lamba92.dragalialost.bot

import com.github.lamba92.dragalialost.data.mappers.AdventurerImageMapper
import com.github.lamba92.dragalialost.kodeindi.dragaliaLostModule
import com.github.lamba92.telegrambots.extensions.TelegramPollingBotProvider
import com.github.lamba92.telegrambots.extensions.telegramBot
import kotlinx.coroutines.flow.toList
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton

object DragaliaBot : TelegramPollingBotProvider {
    override val bot = telegramBot {
        botApiToken = System.getenv("DRAGALIA_TOKEN")
        botUsernameName = "DragaliaBot"

        kodein {
            import(dragaliaLostModule(true))
            bind<AdventurerImageMapper>() with singleton { AdventurerImageMapper(instance()) }
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