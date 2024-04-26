package dev.syoritohatsuki.duckyupdaterrework.client.message

import dev.syoritohatsuki.duckyupdaterrework.core.dto.durw.Printer
import dev.syoritohatsuki.duckyupdaterrework.core.dto.modrinth.AdditionalInfo
import dev.syoritohatsuki.duckyupdaterrework.core.storage.ModId
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun updateAvailable(printer: Printer, additionalInfos: MutableMap<ModId, AdditionalInfo>): Text = Text.empty().apply {
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
                ClickEvent.Action.SUGGEST_COMMAND, "/durw-client update ${printer.projectId}"
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