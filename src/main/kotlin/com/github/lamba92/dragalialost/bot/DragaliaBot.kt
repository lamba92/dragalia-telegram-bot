package com.github.lamba92.dragalialost.bot

import com.github.lamba92.dragalialost.domain.repositories.DragaliaLostRepository
import com.github.lamba92.dragalialost.domain.repositories.searchAdventurers
import com.github.lamba92.dragalialost.kodeindi.dragaliaLostModule
import com.github.lamba92.telegrambots.extensions.TelegramPollingBotProvider
import com.github.lamba92.telegrambots.extensions.content
import com.github.lamba92.telegrambots.extensions.telegramBot
import kotlinx.coroutines.flow.toList
import org.kodein.di.erased.instance
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent

object DragaliaBot : TelegramPollingBotProvider {
    override val bot = telegramBot {
        botApiToken = System.getenv("DRAGALIA_TOKEN")
        botUsernameName = "DragaliaBot"

        kodein {
            import(dragaliaLostModule(true))
        }

        handlers {
            inlineQueries {
                handleQuery { inlineQuery ->

                    val repo by instance<DragaliaLostRepository>()
                    repo.searchAdventurers { name = inlineQuery.query }
                        .mapArticle {
                            content<InputTextMessageContent> {
                                disableWebPagePreview()
                                messageText = it.description
                            }
                            id = it.name
                            title = it.name
                            thumbUrl = it.icons.first()
                            description = "class: ${it.heroClass} | ${it.race} ${it.gender}"
                        }
                        .toList()
                        .let {
                            respond(it) {
                                cacheTime = 0
                            }
                        }

                }
            }
        }
    }
}