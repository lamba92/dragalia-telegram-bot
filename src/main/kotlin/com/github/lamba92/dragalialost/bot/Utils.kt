package com.github.lamba92.dragalialost.bot

import com.github.lamba92.telegrambots.extensions.buildInlineArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle

fun <T> Flow<T>.mapArticle(function: InlineQueryResultArticle.(T) -> Unit) =
    map { buildInlineArticle { function(it) } }