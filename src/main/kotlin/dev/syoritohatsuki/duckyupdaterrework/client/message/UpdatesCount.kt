package dev.syoritohatsuki.duckyupdaterrework.client.message

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun updatesCount(updatesCount: Int): Text = Text.empty().apply {
    append(Text.literal("Available"))
    append(Text.literal(" $updatesCount ").formatted(Formatting.GREEN))
    append(Text.literal("updates from ${FabricLoader.getInstance().allMods.size} installed mods"))
}