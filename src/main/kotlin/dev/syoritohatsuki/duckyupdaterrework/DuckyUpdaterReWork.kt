package dev.syoritohatsuki.duckyupdaterrework

import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.core.util.FileActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DuckyUpdaterReWork : ModInitializer {

    const val MOD_ID = "ducky-updater-rework"

    val logger: Logger = LogManager.getLogger()
    val modVersion: String = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata.version.friendlyString
        ?: DateTimeFormatter.ofPattern("yyyy.M").format(LocalDateTime.now())

    override fun onInitialize() {
        logger.info("Loading common-side DURW")

        ConfigManager
        Database

        CoroutineScope(Dispatchers.IO).launch {
            FileActions.run()
        }
    }
}