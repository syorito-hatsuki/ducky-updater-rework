package dev.syoritohatsuki.duckyupdaterrework.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import net.minecraft.server.command.ServerCommandSource

object UpdateCommand : Command<ServerCommandSource> {
    override fun run(context: CommandContext<ServerCommandSource>): Int {
        DuckyUpdaterReWork.logger.error("ModsIds")
        ModsIdsArgumentType.getModsIds(context, "modsIds").forEachIndexed { index, modId ->
            DuckyUpdaterReWork.logger.error("$index. $modId")
        }
        return 1
    }
}