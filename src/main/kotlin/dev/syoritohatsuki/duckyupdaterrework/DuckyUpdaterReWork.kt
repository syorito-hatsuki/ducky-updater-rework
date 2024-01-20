package dev.syoritohatsuki.duckyupdaterrework

import dev.syoritohatsuki.duckyupdaterrework.core.CommonDuckyUpdaterApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object DuckyUpdaterReWork : ModInitializer {

    const val MOD_ID = "ducky-updater-rework"

    val logger: Logger = LogManager.getLogger()

    override fun onInitialize() {
        CoroutineScope(Dispatchers.IO).launch {
            CommonDuckyUpdaterApi.getUpdates { stringVersionMap ->
                stringVersionMap?.values?.forEach { version ->
                    logger.warn("------------------------")
                    logger.warn(version.name)
                    logger.warn("------------------------")
                    version.changelog.split("\n").forEach {
                        logger.warn(it)
                    }
                    logger.warn("------------------------")
                }
            }
        }
    }
}