package dev.syoritohatsuki.duckyupdater.command

import com.google.gson.JsonObject
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.MOD_ID
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.checkForUpdate
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.getUpdates
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.hashes
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.logger
import dev.syoritohatsuki.duckyupdater.util.diff
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TextContent
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

            getUpdates().forEach { (hash, jsonElement) ->
                val data: JsonObject = jsonElement.asJsonObject
                val metadata: ModMetadata = hashes[hash]!!.metadata

                if (firstLine) {
                    append("")
                    append("Updates available")
                    firstLine = false
                }

                val version = diff(metadata.version.friendlyString, data["version_number"].asString) ?: return@forEach

                append(
                    Text.literal(metadata.name).append(Text.literal(" [").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(version.oldVersion).formatted(Formatting.GRAY))
                        .append(Text.literal(version.matched).formatted(Formatting.RED))
                        .append(Text.literal(" -> ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(version.matched).formatted(Formatting.GRAY))
                        .append(Text.literal(version.newVersion).formatted(Formatting.GREEN))
                        .append(Text.literal("]").formatted(Formatting.DARK_GRAY))
                )
                logger.info("printing...")
            }
            if (!firstLine) append("")
        }, false
    )

    return Command.SINGLE_SUCCESS
}