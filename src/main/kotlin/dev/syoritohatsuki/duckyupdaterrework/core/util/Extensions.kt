package dev.syoritohatsuki.duckyupdaterrework.core.util

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

fun Boolean.toInt(): Int = if (this) 1 else 0

fun SharedSuggestionProvider.sendMessage(vararg textLines: Component) {
    when (this) {
        is CommandSourceStack -> textLines.forEach { player?.sendSystemMessage(it, false) }
        is FabricClientCommandSource -> textLines.forEach {
            CoroutineScope(Minecraft.getInstance().asCoroutineDispatcher()).launch {
                sendFeedback(it)
            }
        }
    }
}

fun SharedSuggestionProvider.sendMessageWithLog(message: String) {
    sendMessage(Component.literal(message))
    if (this is CommandSourceStack) logger.info(message)
}