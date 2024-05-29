package dev.syoritohatsuki.duckyupdaterrework.client.message

import dev.syoritohatsuki.duckyupdaterrework.core.dto.durw.Printer
import dev.syoritohatsuki.duckyupdaterrework.core.dto.modrinth.AdditionalInfo
import dev.syoritohatsuki.duckyupdaterrework.core.storage.ProjectId
import net.minecraft.client.network.ClientCommandSource
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun updateAvailable(
    source: CommandSource, printer: Printer, additionalInfos: MutableMap<ProjectId, AdditionalInfo>
): Text = Text.empty().apply {
    append(Text.literal(printer.prefix))
    append(Text.literal(" [").formatted(Formatting.DARK_GRAY))
    if (printer.currentExist) {
        append(Text.literal(printer.matchedVersion).formatted(Formatting.GRAY))
        append(Text.literal(printer.currentUnMatchVersion).formatted(Formatting.RED))
        append(Text.literal(" -> ").formatted(Formatting.DARK_GRAY))
    }
    append(Text.literal(printer.matchedVersion).formatted(Formatting.GRAY))
    append(Text.literal(printer.newUnMatchVersion).formatted(Formatting.GREEN))
    append(Text.literal("]").formatted(Formatting.DARK_GRAY))
    styled { style ->
        style.withClickEvent(
            ClickEvent(
                ClickEvent.Action.SUGGEST_COMMAND, when (source) {
                    is ServerCommandSource -> "/durw-server update by project-ids ${printer.projectId}"
                    is ClientCommandSource -> "/durw-client update by project-ids ${printer.projectId}"
                    else -> return@styled style
                }
            )
        ).withHoverEvent(
            HoverEvent(
                HoverEvent.Action.SHOW_TEXT, Text.literal(
                    additionalInfos[printer.projectId]?.changeLog ?: return@styled style
                )
            )
        )
    }
}