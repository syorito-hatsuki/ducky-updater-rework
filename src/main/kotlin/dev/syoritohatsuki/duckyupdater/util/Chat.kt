package dev.syoritohatsuki.duckyupdater.util

import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun MutableText.updateListChatMessage(): MutableText {

    val updateVersions = UpdateList.getUpdates()

    if (updateVersions.isEmpty()) return this.append(
        Text.literal("All mods up-to-date")
            .formatted(Formatting.GREEN)
            .formatted(Formatting.BOLD)
    )

    append(
        Text.literal("Updates available")
            .formatted(Formatting.YELLOW)
            .formatted(Formatting.BOLD)
    )

    updateVersions.forEach {
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

fun updateStatusChatMessage(modId: String, status: Int): MutableText = when (status) {
    0 -> Text.literal("Can't update $modId, please check logs!")
        .formatted(Formatting.RED)
        .formatted(Formatting.BOLD)

    1 -> Text.literal("$modId updated successful")
        .formatted(Formatting.GREEN)
        .formatted(Formatting.BOLD)

    else -> somethingWentWrongChatMessage()
}

fun enableUpdateOnStartUpChatMessage(enable: Boolean): MutableText = when (enable) {
    true -> Text.literal("Update on startup enabled")
        .formatted(Formatting.GREEN)
        .formatted(Formatting.BOLD)

    false -> Text.literal("Update on startup disabled")
        .formatted(Formatting.RED)
        .formatted(Formatting.BOLD)

}

fun somethingWentWrongChatMessage(): MutableText = Text.literal("Something went wrong :(")
    .formatted(Formatting.RED)
    .formatted(Formatting.BOLD)

fun ignoreUpdateChatMessage(modId: String, version: String): MutableText =
    Text.literal("$modId update $version added to ignore")
        .formatted(Formatting.GREEN)
        .formatted(Formatting.BOLD)

fun nothingToIgnoreChatMessage(): MutableText =
    Text.literal("Update not found for ignoring")
        .formatted(Formatting.RED)
        .formatted(Formatting.BOLD)