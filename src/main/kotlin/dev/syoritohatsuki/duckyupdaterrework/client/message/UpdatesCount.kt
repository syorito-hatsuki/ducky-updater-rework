package dev.syoritohatsuki.duckyupdaterrework.client.message

import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Loader
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

fun updatesCount(updatesCount: Int, loader: Loader): Text = Text.empty().apply {
    append(Text.literal("Available"))
    append(Text.literal(" $updatesCount ").formatted(Formatting.GREEN))
    when (loader) {
        Loader.FABRIC -> append(Text.literal("updates from ${FabricLoader.getInstance().allMods.size} installed mods"))
        Loader.DATAPACK -> {
            append(Text.literal("updates from "))
            append(
                Text.literal(
                    DuckyUpdaterApi.defaultDatapacksDir?.listDirectoryEntries()
                        ?.filter { it.isRegularFile() }?.size?.toString() ?: "?"
                ).styled {
                    it.withItalic(true).withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Text.literal("Just count files in directory (even if not datapack file)")
                        )
                    )
                })
            append(Text.literal(" installed datapacks"))
        }
    }
}
