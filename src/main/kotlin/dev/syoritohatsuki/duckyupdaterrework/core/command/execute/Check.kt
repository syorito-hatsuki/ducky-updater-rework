package dev.syoritohatsuki.duckyupdaterrework.core.command.execute

import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.client.message.updateAvailable
import dev.syoritohatsuki.duckyupdaterrework.client.message.updatesCount
import dev.syoritohatsuki.duckyupdaterrework.server.message.updateAvailable
import dev.syoritohatsuki.duckyupdaterrework.server.message.updatesCount
import dev.syoritohatsuki.duckyupdaterrework.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.util.buildModsTree
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text


fun check(context: CommandContext<out CommandSource>): Int {
    val modsIds = Database.modsIds()
    val additionalInfos = Database.additionalInfoByModsIds(modsIds)

    var updatesCount: Int

    buildModsTree(modsIds, additionalInfos).onEach { printer ->
        when (context.source) {
            is ServerCommandSource -> logger.updateAvailable(printer)
            is FabricClientCommandSource -> (context.source as FabricClientCommandSource).sendFeedback(
                updateAvailable(printer, additionalInfos)
            )
        }
    }.also {
        updatesCount = it.map { printer -> printer.projectId }.toHashSet().size
    }

    when (context.source) {
        is ServerCommandSource -> logger.apply {
            info("")
            updatesCount(updatesCount)
        }

        is FabricClientCommandSource -> (context.source as FabricClientCommandSource).apply {
            sendFeedback(Text.empty())
            sendFeedback(updatesCount(updatesCount))
        }
    }

    return 1
}