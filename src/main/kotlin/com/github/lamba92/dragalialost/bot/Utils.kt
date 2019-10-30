package com.github.lamba92.dragalialost.bot

import com.github.lamba92.dragalialost.domain.entities.AdventurerEntity
import com.github.lamba92.dragalialost.domain.entities.DragaliaEntity
import com.github.lamba92.dragalialost.domain.entities.DragonEntity
import com.github.lamba92.dragalialost.domain.entities.WyrmprintEntity
import com.github.lamba92.dragalialost.domain.entities.enums.Rarity
import com.github.lamba92.dragalialost.domain.repositories.DragaliaLostRepository
import com.github.lamba92.dragalialost.domain.usecases.SearchAllByNameUseCase
import com.github.lamba92.telegrambots.extensions.InlineQueryReceivedHandler
import com.github.lamba92.telegrambots.extensions.buildInlineArticle
import com.github.lamba92.telegrambots.extensions.content
import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.kodein.di.direct
import org.kodein.di.erased.instance
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle

fun <T> Flow<T>.mapArticle(function: InlineQueryResultArticle.(T) -> Unit) =
    map { buildInlineArticle { function(it) } }

fun Flow<DragaliaEntity>.mapArticle() = mapArticle {
    content<InputTextMessageContent> {
        messageText = buildMarkdownMessage(it)
        enableMarkdown(true)
    }
    thumbUrl = it.icon
    id = it.name
    title = "${it.name} | ${it::class.simpleName!!.removeSuffix("Entity")}"
    thumbUrl = it.icon
    description = it.baseRarity.printStars() +
            "\n${it.hp.toString().padStart(4)} HPs - ${it.strength.toString().padStart(4)} STR - " +
            "${it.baseMaxMight.toString().padStart(4)} Might"
}

private fun Rarity.printStars() = buildStringWithEmojiis {
    when (this@printStars) {
        Rarity.TWO -> append(":star::star:")
        Rarity.THREE -> append(":star::star::star:")
        Rarity.FOUR -> append(":star::star::star::star:")
        Rarity.FIVE -> append(":star::star::star::star::star:")
    }
}

fun buildStringWithEmojiis(function: StringBuilder.() -> Unit) =
    EmojiParser.parseToUnicode(buildString(function))!!

fun buildMarkdownMessage(entity: DragaliaEntity) = when (entity) {
    is AdventurerEntity -> buildForAdventurer(entity)
    is DragonEntity -> buildForDragon(entity)
    is WyrmprintEntity -> buildForWyrmprint(entity)
    else -> throw IllegalArgumentException("Entity of class ${entity::class.simpleName} has not been handled yet")
}

fun buildForWyrmprint(entity: WyrmprintEntity) = buildString {
    append("![Image](${entity.artwork})")
}

fun buildForDragon(entity: DragonEntity) = buildString {
    append("![Image](${entity.artwork})")
}

fun buildForAdventurer(entity: AdventurerEntity) = buildString {
    append("![Image](${entity.artwork})")
}

val InlineQueryReceivedHandler.dragaliaRepository
    get() = direct.instance<DragaliaLostRepository>()

val InlineQueryReceivedHandler.searchAllUseCase
    get() = direct.instance<SearchAllByNameUseCase>()

