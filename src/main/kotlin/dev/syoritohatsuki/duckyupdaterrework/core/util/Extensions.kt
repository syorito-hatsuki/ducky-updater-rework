package dev.syoritohatsuki.duckyupdaterrework.core.util

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

fun Boolean.toInt(): Int = if (this) 1 else 0

fun CommandSource.sendMessage(vararg textLines: Text) {
    when (this) {
        is ServerCommandSource -> textLines.map { player?.sendMessage(it, false) }
        is FabricClientCommandSource -> textLines.map(::sendFeedback)
    }
}

fun CommandSource.sendMessageWithLog(message: String) {
    sendMessage(Text.literal(message))
    if (this is ServerCommandSource) logger.info(message)
}