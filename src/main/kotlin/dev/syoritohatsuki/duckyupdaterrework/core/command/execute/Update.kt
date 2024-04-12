package dev.syoritohatsuki.duckyupdaterrework.core.command.execute

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import net.minecraft.command.CommandSource

fun update(context: CommandContext<out CommandSource>): Int {
    val modsIds = runCatching {
        ModsIdsArgumentType.getModsIds(context, "modsIds")
    }.getOrElse {
        StringArgumentType.getString(context, "modsIdsString").split(" ")
    }



    return 1
}