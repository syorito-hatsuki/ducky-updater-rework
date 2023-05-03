package dev.syoritohatsuki.duckyupdater.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.MOD_ID
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.checkForUpdate
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.updateVersions
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.*
import net.minecraft.util.Formatting
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels


fun CommandDispatcher<ServerCommandSource>.serverSideCommands() {
    listOf("du", MOD_ID).forEach { rootLiteral ->
        register(
            LiteralArgumentBuilder.literal<ServerCommandSource>(rootLiteral)
                .then(LiteralArgumentBuilder.literal<ServerCommandSource>("check-for-updates")
                    .executes { it.executeCheckForUpdates() })
                .then(
                    LiteralArgumentBuilder.literal<ServerCommandSource>("download").then(
                        CommandManager.argument("modId", StringArgumentType.word()).executes {
                            it.executeDownloadUpdates()
                        }
                    )
                )
        )
    }
}

private fun CommandContext<ServerCommandSource>.executeCheckForUpdates(): Int {

    source.sendFeedback(
        MutableText.of(TextContent.EMPTY).apply {
            var firstLine = true

            checkForUpdate()

            updateVersions.forEach {

                if (firstLine) {
                    append(Text.literal("Updates available").formatted(Formatting.YELLOW).formatted(Formatting.BOLD))
                    firstLine = false
                }

                append(
                    Text.literal("\n - ${it.modName} ")
                        .append(Text.literal("[").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(it.versions.matched).formatted(Formatting.GRAY))
                        .append(Text.literal(it.versions.oldVersion).formatted(Formatting.RED))
                        .append(Text.literal(" -> ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(it.versions.matched).formatted(Formatting.GRAY))
                        .append(Text.literal(it.versions.newVersion).formatted(Formatting.GREEN))
                        .append(Text.literal("]").formatted(Formatting.DARK_GRAY)).styled { style ->
                            style.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, it.url))
                                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(it.changeLog)))
                        }
                )
            }
        }, false
    )

    return Command.SINGLE_SUCCESS
}

private fun CommandContext<ServerCommandSource>.executeDownloadUpdates(): Int {

    updateVersions.first { it.modId == StringArgumentType.getString(this, "modId") }.let {
        try {
            FileOutputStream(
                File(
                    it.modPath.parent.toFile(),
                    it.remoteFileName
                )
            ).channel.transferFrom(Channels.newChannel(URL(it.url).openStream()), 0, Long.MAX_VALUE)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
        it.modPath.toFile().delete()
    }

    return Command.SINGLE_SUCCESS
}