package dev.syoritohatsuki.duckyupdater.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.MOD_ID
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.checkForUpdate
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.updateVersions
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.*
import net.minecraft.util.Formatting

fun CommandDispatcher<ServerCommandSource>.serverSideCommands() {
    listOf("du", MOD_ID).forEach { rootLiteral ->
        register(
            LiteralArgumentBuilder.literal<ServerCommandSource>(rootLiteral)
                .then(LiteralArgumentBuilder.literal<ServerCommandSource>("check-for-updates")
                    .executes { it.executeCheckForUpdates() })
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