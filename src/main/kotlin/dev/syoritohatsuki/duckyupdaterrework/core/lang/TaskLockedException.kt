package dev.syoritohatsuki.duckyupdaterrework.core.lang

import dev.syoritohatsuki.duckyupdaterrework.server.message.BRIGHT_RED
import dev.syoritohatsuki.duckyupdaterrework.server.message.GRAY
import dev.syoritohatsuki.duckyupdaterrework.server.message.RESET
import dev.syoritohatsuki.duckyupdaterrework.server.message.YELLOW
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// Class package selected base on Java Core packages
class TaskLockedException(private val task: String) :
    RuntimeException("${BRIGHT_RED}Task Manager is locked by $GRAY[$YELLOW${task}$GRAY]$RESET") {
    fun getMinecraftText(): Text = Text.empty().apply {
        append(Text.literal("Task Manager is locked by ").formatted(Formatting.RED))
        append(Text.literal("[").formatted(Formatting.DARK_GRAY))
        append(Text.literal(task).formatted(Formatting.YELLOW))
        append(Text.literal("]").formatted(Formatting.DARK_GRAY))
    }
}