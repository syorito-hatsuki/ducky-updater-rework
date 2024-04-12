package dev.syoritohatsuki.duckyupdaterrework.server.message

import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.Logger

fun Logger.updatesCount(updatesCount: Int) =
    info("Available ${BRIGHT_GREEN}$updatesCount${RESET} updates from ${FabricLoader.getInstance().allMods.size} installed mods")