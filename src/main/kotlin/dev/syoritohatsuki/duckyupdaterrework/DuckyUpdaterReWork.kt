package dev.syoritohatsuki.duckyupdaterrework

import dev.syoritohatsuki.duckyupdaterrework.util.Downloader
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object DuckyUpdaterReWork : ModInitializer {

    const val MOD_ID = "ducky-updater-rework"

    val logger: Logger = LogManager.getLogger()

    override fun onInitialize() {
        logger.info("Loading common-side DURW")
        Downloader
    }
}