package dev.syoritohatsuki.duckyupdaterrework.core.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import dev.syoritohatsuki.duckyupdaterrework.util.Hash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.command.CommandSource

object UpdateCommand {

    fun update(context: CommandContext<out CommandSource>): Int {
        val modsIds = runCatching {
            ModsIdsArgumentType.getModsIds(context, "modsIds")
        }.getOrElse {
            StringArgumentType.getString(context, "modsIdsString").split(" ")
        }

        CoroutineScope(Dispatchers.IO).launch {
            DuckyUpdaterReWork.logger.error("ModsIds")
            DuckyUpdaterApi.checkForUpdates(Hash.getSha512Hashes(modsIds))

            DuckyUpdaterApi.getUpdatesUrls().onEachIndexed { index, entry ->
                DuckyUpdaterReWork.logger.error("$index. ${entry.key} ${entry.value}")
            }
        }

        return 1
    }

    fun updateAll(@Suppress("UNUSED_PARAMETER") context: CommandContext<out CommandSource>): Int {

        CoroutineScope(Dispatchers.IO).launch {
            DuckyUpdaterApi.checkForUpdates()

            DuckyUpdaterApi.getUpdatesUrls().onEachIndexed { index, entry ->
                DuckyUpdaterReWork.logger.error("$index. ${entry.key} ${entry.value}")
            }
        }

        return 1
    }
}