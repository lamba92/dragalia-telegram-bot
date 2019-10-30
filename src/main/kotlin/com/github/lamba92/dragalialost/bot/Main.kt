package com.github.lamba92.dragalialost.bot

import com.github.lamba92.telegrambots.extensions.KApiContextInitializer
import com.github.lamba92.telegrambots.extensions.registerBot

fun main() = KApiContextInitializer {
    registerBot(DragaliaBot)
}



