package dev.syoritohatsuki.duckyupdaterrework

import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Paths

object DuckyUpdaterReWork : ModInitializer {

    const val MOD_ID = "ducky-updater-rework"

    val logger: Logger = LogManager.getLogger()
    val configDir: File = Paths.get("", "config", MOD_ID).toFile()

    override fun onInitialize() {
        logger.info("Loading common-side DURW")
    }
}