package dev.syoritohatsuki.duckyupdaterrework.client.message

import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Loader
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

fun updatesCount(updatesCount: Int, loader: Loader): Component = Component.empty().apply {
    append(Component.literal("Available"))
    append(Component.literal(" $updatesCount ").withStyle(ChatFormatting.GREEN))
    when (loader) {
        Loader.FABRIC -> append(Component.literal("updates from ${FabricLoader.getInstance().allMods.size} installed mods"))
        Loader.DATAPACK -> {
            append(Component.literal("updates from "))
            append(
                Component.literal(
                    DuckyUpdaterApi.defaultDatapacksDir?.listDirectoryEntries()
                        ?.filter { it.isRegularFile() }?.size?.toString() ?: "?"
                ).withStyle {
                    it.withItalic(true).withHoverEvent(
                        HoverEvent.ShowText(
                            Component.literal("Just count files in directory (even if not datapack file)")
                        )
                    )
                })
            append(Component.literal(" installed datapacks"))
        }
    }
}
