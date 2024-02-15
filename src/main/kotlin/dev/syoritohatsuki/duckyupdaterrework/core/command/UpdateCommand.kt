package dev.syoritohatsuki.duckyupdaterrework.core.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import net.minecraft.command.CommandSource

object UpdateCommand {
    fun update(context: CommandContext<out CommandSource>): Int {
        val modsIds = runCatching {
            ModsIdsArgumentType.getModsIds(context, "modsIds")
        }.getOrElse {
            StringArgumentType.getString(context, "modsIdsString").split(" ")
        }

        DuckyUpdaterReWork.logger.error("ModsIds")
        modsIds.forEachIndexed { index, modId ->
            DuckyUpdaterReWork.logger.error("$index. $modId")
        }

        return 1
    }

    fun updateAll(@Suppress("UNUSED_PARAMETER") context: CommandContext<out CommandSource>): Int {
        DuckyUpdaterReWork.logger.error("ModsIds")
        DuckyUpdaterReWork.modsIds.forEachIndexed { index, modId ->
            DuckyUpdaterReWork.logger.error("$index. $modId")
        }
        return 1
    }
}