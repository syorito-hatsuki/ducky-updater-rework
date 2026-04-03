package dev.syoritohatsuki.duckyupdaterrework.client.message

import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Loader
import dev.syoritohatsuki.duckyupdaterrework.core.dto.durw.Printer
import dev.syoritohatsuki.duckyupdaterrework.core.dto.modrinth.AdditionalInfo
import dev.syoritohatsuki.duckyupdaterrework.core.storage.ProjectId
import net.minecraft.ChatFormatting
import net.minecraft.client.multiplayer.ClientSuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent

fun updateAvailable(
    source: SharedSuggestionProvider, printer: Printer, additionalInfos: MutableMap<ProjectId, AdditionalInfo>, loader: Loader
): Component = Component.empty().apply {
    append(Component.literal(printer.prefix))
    append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY))
    if (printer.currentExist) {
        append(Component.literal(printer.matchedVersion).withStyle(ChatFormatting.GRAY))
        append(Component.literal(printer.currentUnMatchVersion).withStyle(ChatFormatting.RED))
        append(Component.literal(" -> ").withStyle(ChatFormatting.DARK_GRAY))
    }
    append(Component.literal(printer.matchedVersion).withStyle(ChatFormatting.GRAY))
    append(Component.literal(printer.newUnMatchVersion).withStyle(ChatFormatting.GREEN))
    append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY))
    withStyle { style ->
        style.withClickEvent(
            ClickEvent.SuggestCommand(
                when (source) {
                    is CommandSourceStack -> "/durw-server update by ${loader.name.lowercase()}-ids ${printer.projectId}"
                    is ClientSuggestionProvider -> "/durw-client update by ${loader.name.lowercase()}-ids ${printer.projectId}"
                    else -> return@withStyle style
                }
            )
        ).withHoverEvent(
            HoverEvent.ShowText(
                Component.literal(
                    additionalInfos[printer.projectId]?.changeLog ?: return@withStyle style
                )
            )
        )
    }
}
