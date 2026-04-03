package dev.syoritohatsuki.duckyupdaterrework.core.lang

import dev.syoritohatsuki.duckyupdaterrework.server.message.BRIGHT_RED
import dev.syoritohatsuki.duckyupdaterrework.server.message.GRAY
import dev.syoritohatsuki.duckyupdaterrework.server.message.RESET
import dev.syoritohatsuki.duckyupdaterrework.server.message.YELLOW
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

// Class package selected base on Java Core packages
class TaskLockedException(private val task: String) :
    RuntimeException("${BRIGHT_RED}Task Manager is locked by $GRAY[$YELLOW${task}$GRAY]$RESET") {
    fun getMinecraftText(): Component = Component.empty().apply {
        append(Component.literal("Task Manager is locked by ").withStyle(ChatFormatting.RED))
        append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
        append(Component.literal(task).withStyle(ChatFormatting.YELLOW))
        append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY))
    }
}