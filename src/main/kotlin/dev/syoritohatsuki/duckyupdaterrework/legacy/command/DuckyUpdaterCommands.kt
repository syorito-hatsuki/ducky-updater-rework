package dev.syoritohatsuki.duckyupdaterrework.legacy.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.MOD_ID
import dev.syoritohatsuki.duckyupdaterrework.legacy.DuckyUpdaterReWork.checkForUpdate
import dev.syoritohatsuki.duckyupdaterrework.legacy.DuckyUpdaterReWork.updateAll
import dev.syoritohatsuki.duckyupdaterrework.legacy.DuckyUpdaterReWork.updateByModId
import dev.syoritohatsuki.duckyupdaterrework.legacy.util.*
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.TextContent

fun CommandDispatcher<ServerCommandSource>.serverSideCommands() {
    listOf("du", MOD_ID).forEach { rootLiteral ->
        register(
            LiteralArgumentBuilder.literal<ServerCommandSource>(rootLiteral).requires {
                it.hasPermissionLevel(4)
            }.executes { it.executeListAvailableUpdates() }
                .then(LiteralArgumentBuilder.literal<ServerCommandSource>("check-for-updates")
                    .executes { it.executeCheckForUpdates() })
                .then(LiteralArgumentBuilder.literal<ServerCommandSource?>("update-on-startup").then(
                    CommandManager.argument("enable", BoolArgumentType.bool())
                        .executes { it.executeEnableUpdateOnStartUp() }
                ))
                .then(LiteralArgumentBuilder.literal<ServerCommandSource>("update").then(
                    CommandManager.argument("modId", StringArgumentType.word())
                        .executes { it.executeUpdate() }
                ).then(
                    LiteralArgumentBuilder.literal<ServerCommandSource?>("all")
                        .executes { it.executeUpdateAll() }
                )).then(LiteralArgumentBuilder.literal<ServerCommandSource?>("ignore").then(
                    CommandManager.argument("modId", StringArgumentType.word())
                        .executes { it.executeIgnoreUpdate() }
                ))
        )
    }
}

private fun CommandContext<ServerCommandSource>.executeListAvailableUpdates(): Int {
    if (source.player == null) DuckyUpdaterReWork.updateListCliMessage() else source.sendFeedback({
        MutableText.of(TextContent.EMPTY).updateListChatMessage()
    }, false)

    return Command.SINGLE_SUCCESS
}

private fun CommandContext<ServerCommandSource>.executeCheckForUpdates(): Int {

    checkForUpdate()

    if (source.player == null) DuckyUpdaterReWork.updateListCliMessage() else source.sendFeedback({
        MutableText.of(TextContent.EMPTY).updateListChatMessage()
    }, false)

    return Command.SINGLE_SUCCESS
}

private fun CommandContext<ServerCommandSource>.executeUpdate(): Int {
    val modId = StringArgumentType.getString(this, "modId")

    updateByModId(modId, source)

    return Command.SINGLE_SUCCESS
}

private fun CommandContext<ServerCommandSource>.executeUpdateAll(): Int {
    updateAll(source)
    return Command.SINGLE_SUCCESS
}

private fun CommandContext<ServerCommandSource>.executeIgnoreUpdate(): Int {
    UpdateList.getUpdates().find { it.modId == StringArgumentType.getString(this, "modId") }?.let {
        val version = "${it.versions.matched}${it.versions.newVersion}"

        ConfigManager.addVersionToIgnore(it.modId, version)

        if (source.player == null) ignoreUpdateCliMessage(it.modId, version) else source.sendFeedback({
            ignoreUpdateChatMessage(it.modId, version)
        }, false)

    } ?: if (source.player == null) nothingToIgnoreCliMessage() else source.sendFeedback({
        nothingToIgnoreChatMessage()
    }, false)

    return Command.SINGLE_SUCCESS
}

private fun CommandContext<ServerCommandSource>.executeEnableUpdateOnStartUp(): Int {
    val enable = BoolArgumentType.getBool(this, "enable")

    ConfigManager.changeUpdateOnStartUp(enable)

    if (source.player == null) enableUpdateOnStartUpCliMessage(enable) else source.sendFeedback({
        enableUpdateOnStartUpChatMessage(enable)
    }, false)

    return Command.SINGLE_SUCCESS
}