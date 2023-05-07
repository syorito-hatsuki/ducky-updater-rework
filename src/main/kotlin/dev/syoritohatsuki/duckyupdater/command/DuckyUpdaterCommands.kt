package dev.syoritohatsuki.duckyupdater.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.MOD_ID
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.checkForUpdate
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.updateAll
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.updateByModId
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.updateVersions
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.*
import net.minecraft.util.Formatting

fun CommandDispatcher<ServerCommandSource>.serverSideCommands() {
    listOf("du", MOD_ID).forEach { rootLiteral ->
        register(
            LiteralArgumentBuilder.literal<ServerCommandSource>(rootLiteral)
                .then(LiteralArgumentBuilder.literal<ServerCommandSource>("check-for-updates")
                    .executes { it.executeCheckForUpdates() })
                .then(LiteralArgumentBuilder.literal<ServerCommandSource>("update").then(
                    CommandManager.argument("modId", StringArgumentType.word())
                        .executes { it.executeUpdate() }
                ).then(LiteralArgumentBuilder.literal<ServerCommandSource?>("all")
                    .executes { it.executeUpdateAll() }
                ))
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
                    append(
                        Text.literal("Updates available")
                            .formatted(Formatting.YELLOW)
                            .formatted(Formatting.BOLD)
                    )
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
                            style.withClickEvent(
                                ClickEvent(
                                    ClickEvent.Action.SUGGEST_COMMAND,
                                    "/du update ${it.modId}"
                                )
                            ).withHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text.literal(it.changeLog)
                                )
                            )
                        }
                )
            }
        }, false
    )

    return Command.SINGLE_SUCCESS
}

private fun CommandContext<ServerCommandSource>.executeUpdate(): Int {
    val modId = StringArgumentType.getString(this, "modId")
    updateByModId(modId).let {
        when (it) {
            0 -> source.sendFeedback(
                Text.literal("Can't update $modId, please check logs!")
                    .formatted(Formatting.RED)
                    .formatted(Formatting.BOLD),
                false
            )

            1 -> source.sendFeedback(
                Text.literal("$modId updated successful")
                    .formatted(Formatting.GREEN)
                    .formatted(Formatting.BOLD),
                false
            )
        }
    }
    return Command.SINGLE_SUCCESS
}

private fun CommandContext<ServerCommandSource>.executeUpdateAll(): Int {
    updateAll().forEach {
        when (it.value) {
            0 -> source.sendFeedback(
                Text.literal("Can't update ${it.key}, please check logs!")
                    .formatted(Formatting.RED)
                    .formatted(Formatting.BOLD),
                false
            )

            1 -> source.sendFeedback(
                Text.literal("${it.key} updated successful")
                    .formatted(Formatting.GREEN)
                    .formatted(Formatting.BOLD),
                false
            )
        }
    }
    return Command.SINGLE_SUCCESS
}