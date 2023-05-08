package dev.syoritohatsuki.duckyupdater.util

import dev.syoritohatsuki.duckyupdater.DuckyUpdater.updateVersions
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun MutableText.updateListChatMessage(): MutableText {
    var firstLine = true
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
    return this
}

fun MutableText.updateStatusChatMessage(modId: String, status: Int): MutableText {
    return when (status) {
        0 -> Text.literal("Can't update $modId, please check logs!")
            .formatted(Formatting.RED)
            .formatted(Formatting.BOLD)

        1 -> Text.literal("$modId updated successful")
            .formatted(Formatting.GREEN)
            .formatted(Formatting.BOLD)

        else -> this
    }
}