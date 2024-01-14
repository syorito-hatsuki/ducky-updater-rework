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
            CommonDuckyUpdaterApi.getUpdate("fstats-api") {
                logger.info(it?.changelog ?: "Is null :(")
            }
        }
    }
}