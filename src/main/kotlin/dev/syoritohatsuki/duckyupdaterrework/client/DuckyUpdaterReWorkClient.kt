package dev.syoritohatsuki.duckyupdaterrework.client

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import net.fabricmc.api.ClientModInitializer

object DuckyUpdaterReWorkClient : ClientModInitializer {
    override fun onInitializeClient() {
        logger.info("Loading client-side DURW")
    }
}