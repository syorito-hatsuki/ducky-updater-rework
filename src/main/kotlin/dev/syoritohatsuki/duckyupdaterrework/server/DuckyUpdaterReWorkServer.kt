package dev.syoritohatsuki.duckyupdaterrework.server

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import net.fabricmc.api.DedicatedServerModInitializer

object DuckyUpdaterReWorkServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        logger.info("Loading server-side DURW")
    }
}