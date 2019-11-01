package com.github.lamba92.dragalialost.bot

import com.github.lamba92.dragalialost.domain.entities.AdventurerEntity
import com.github.lamba92.dragalialost.domain.entities.DragaliaEntity
import com.github.lamba92.dragalialost.domain.entities.DragonEntity
import com.github.lamba92.dragalialost.domain.entities.WyrmprintEntity
import com.github.lamba92.dragalialost.domain.entities.enums.AbilityLevel
import com.github.lamba92.dragalialost.domain.entities.enums.CoAbilityLevel
import com.github.lamba92.dragalialost.domain.entities.enums.Rarity
import com.github.lamba92.dragalialost.domain.entities.enums.SkillLevel
import com.github.lamba92.dragalialost.domain.entities.support.*
import com.github.lamba92.dragalialost.domain.repositories.DragaliaLostRepository
import com.github.lamba92.dragalialost.domain.usecases.SearchAllByNameUseCase
import com.github.lamba92.telegrambots.extensions.*
import com.github.lamba92.telegrambots.extensions.TelegramMarkdownBuilder.Style.BOLD
import com.github.lamba92.telegrambots.extensions.TelegramMarkdownBuilder.Style.INLINE_CODE
import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.kodein.di.direct
import org.kodein.di.erased.instance
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle

fun <T> Flow<T>.mapArticle(function: InlineQueryResultArticle.(T) -> Unit) =
    map { buildInlineArticle { function(it) } }

fun Flow<DragaliaEntity>.mapArticle() = mapArticle { entity ->
    content<InputTextMessageContent> {
        messageText = buildMarkdownMessage(entity).also {
            println("MESSAGE BUILT FOR ${entity.name}:\n$it")
        }
        enableMarkdown(true)
    }
    thumbUrl = entity.icon
    id = entity.name
    title = "${entity.name} | ${entity::class.simpleName!!.removeSuffix("Entity")}"
    thumbUrl = entity.icon
    description = entity.baseRarity.printStars() +
            "\n${entity.hp.toString().padStart(4)} HPs - ${entity.strength.toString().padStart(4)} STR - " +
            "${entity.baseMaxMight.toString().padStart(4)} Might"
}

fun Rarity.printStars() = buildStringWithEmojiis {
    when (this@printStars) {
        Rarity.TWO -> append(":star::star:")
        Rarity.THREE -> append(":star::star::star:")
        Rarity.FOUR -> append(":star::star::star::star:")
        Rarity.FIVE -> append(":star::star::star::star::star:")
    }
}

fun buildStringWithEmojiis(function: StringBuilder.() -> Unit) =
    EmojiParser.parseToUnicode(buildString(function))!!

fun buildMarkdownWithEmojiis(function: TelegramMarkdownBuilder.() -> Unit) =
    EmojiParser.parseToUnicode(buildMarkdown(function))!!

fun buildMarkdownMessage(entity: DragaliaEntity) = buildMarkdownWithEmojiis {
    append(entity::class.simpleName!!.removeSuffix("Entity"))
    append(": ")
    appendln(entity.name, BOLD)
    appendln("Rarity: ${entity.baseRarity.number} | ${entity.baseRarity.printStars()}")
    append(entity.hp, BOLD)
    append(" HPs | ")
    append(entity.strength, BOLD)
    append(" STR | ")
    append(entity.baseMaxMight, BOLD)
    appendln(" Might")
    appendImage(entity.artwork, "Artwork")
    appendln()
    appendln()
    appendln("Details:", BOLD)
    appendln(" - max level: ${entity.maxLevel}")
    appendln(" - base minimum might: ${entity.baseMinMight}")
    appendln(" - obtained from:")
    entity.obtainedFrom.forEach {
        appendln("   • $it")
    }
    appendln(" - release date: ${entity.releaseDate.toString("dd/MM/yyyy")}")
    appendEntitySpecificData(entity)
}

fun TelegramMarkdownBuilder.appendEntitySpecificData(entity: DragaliaEntity) {
    when (entity) {
        is AdventurerEntity -> {
            appendln(" - ${entity.race.name.toLowerCase()} ${entity.gender.name.toLowerCase()}")
            appendln(" - element: ${entity.element.name}")
            appendln(" - weapon: ${entity.weaponType.name}")
            appendln(" - class: ${entity.heroClass.name}")
            appendln()
            appendln("ABILITIES", INLINE_CODE)
            appendAbility(entity.ability1)
            entity.ability2?.let { appendAbility(it) }
            entity.ability3?.let { appendAbility(it) }
            appendln()
            appendln("SKILLS", INLINE_CODE)
            appendSkill(entity.skill1)
            appendSkill(entity.skill2)
            appendln()
            appendln("CoAbility", INLINE_CODE)
            appendCoAbility(entity.coAbility)
        }
        is DragonEntity -> {
            appendln(" - element: ${entity.element.name}")
            appendln(" - sell value: ${entity.sellValue}")
            appendln()
            appendln("ABILITIES", INLINE_CODE)
            entity.ability1?.let { appendAbility(it) }
            entity.ability2?.let { appendAbility(it) }
            appendln()
            appendln("SKILL", INLINE_CODE)
            appendSkill(entity.skill)
        }
        is WyrmprintEntity -> {
            appendln(" - sell value: ${entity.sellValue}")
            appendln()
            appendln("ABILITIES", INLINE_CODE)
            appendAbility(entity.ability1)
            entity.ability2?.let { appendAbility(it) }
            entity.ability3?.let { appendAbility(it) }
        }
        else -> {
        }
    }
}

private fun TelegramMarkdownBuilder.appendAbility(ability: WyrmprintAbility) {
    append(" - ${ability.name}", BOLD)
    appendln(" [[icon](${ability.icon})]:")
    printAbilityLevelData(ability.level1)
    printAbilityLevelData(ability.level2)
    printAbilityLevelData(ability.level3)
}

fun TelegramMarkdownBuilder.appendAbility(ability: DragonAbility) {
    append(" - ${ability.name}", BOLD)
    appendln(" [[icon](${ability.icon})]:")
    printAbilityLevelData(ability.level1)
    printAbilityLevelData(ability.level2)
}

fun TelegramMarkdownBuilder.appendSkill(skill: DragonSkill) {
    append(" - ${skill.name}", BOLD)
    appendln(" [[icon](${skill.icon})]:")
    appendln("   • SP cost: ${skill.skillPointCost}")
    printSkillLevelData(skill.level1)
    printSkillLevelData(skill.level2)
}

fun TelegramMarkdownBuilder.printSkillLevelData(data: SkillLevelData) {
    append("   • ")
    append("LVL${data.level.number}", INLINE_CODE)
    appendln(": ${data.description}")
}

fun TelegramMarkdownBuilder.printAbilityLevelData(data: AbilityLevelData) {
    append("   • ")
    append("LVL${data.level.number}", INLINE_CODE)
    appendln(": ${data.description.takeAsString()}")
}

fun TelegramMarkdownBuilder.printCoAbilityLevelData(data: CoAbilityLevelData) {
    append("   • ")
    append("LVL${data.level.number}", INLINE_CODE)
    appendln(": ${data.description.takeAsString()}")
}

fun TelegramMarkdownBuilder.appendSkill(skill: AdventurerSkill) {
    append(" - ${skill.name}", BOLD)
    appendln(" [[icon](${skill.icon})]:")
    appendln("   • SP cost: ${skill.skillPointCost}")
    printSkillLevelData(skill.level1)
    printSkillLevelData(skill.level2)
    skill.level3?.let { printSkillLevelData(it) }
}

fun TelegramMarkdownBuilder.appendAbility(ability: AdventurerAbility) {
    append(" - ${ability.name}", BOLD)
    appendln(" [[icon](${ability.icon})]:")
    printAbilityLevelData(ability.level1)
    ability.level2?.let { printAbilityLevelData(it) }
    ability.level3?.let { printAbilityLevelData(it) }
}

fun TelegramMarkdownBuilder.appendCoAbility(coAbility: CoAbility) {
    append(" - ${coAbility.name}", BOLD)
    appendln(" [[icon](${coAbility.icon})]:")
    printCoAbilityLevelData(coAbility.level1)
    printCoAbilityLevelData(coAbility.level2)
    printCoAbilityLevelData(coAbility.level3)
    printCoAbilityLevelData(coAbility.level4)
    printCoAbilityLevelData(coAbility.level5)
}

fun <T> T.takeAsString(i: Int = 150) =
    toString().let { if (it.length >= i + 3) it.take(i) + "..." else it }

val CoAbilityLevel.number
    get() = when (this) {
        CoAbilityLevel.ONE -> 1
        CoAbilityLevel.TWO -> 2
        CoAbilityLevel.THREE -> 3
        CoAbilityLevel.FOUR -> 4
        CoAbilityLevel.FIVE -> 5
    }

val AbilityLevel.number
    get() = when (this) {
        AbilityLevel.ONE -> 1
        AbilityLevel.TWO -> 2
        AbilityLevel.THREE -> 3
    }

val SkillLevel.number: Int
    get() = when (this) {
        SkillLevel.ONE -> 1
        SkillLevel.TWO -> 2
        SkillLevel.THREE -> 3
    }

val Rarity.number
    get() = when (this) {
        Rarity.TWO -> 2
        Rarity.THREE -> 3
        Rarity.FOUR -> 4
        Rarity.FIVE -> 5
    }


val InlineQueryReceivedHandler.dragaliaRepository
    get() = direct.instance<DragaliaLostRepository>()

val InlineQueryReceivedHandler.searchAllUseCase
    get() = direct.instance<SearchAllByNameUseCase>()

