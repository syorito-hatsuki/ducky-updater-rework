package dev.syoritohatsuki.duckyupdaterrework

import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.core.util.FileActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModOrigin
import net.fabricmc.loader.impl.util.LoaderUtil
import net.fabricmc.loader.impl.util.SystemProperties
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.jvm.optionals.getOrNull

object DuckyUpdaterReWork : ModInitializer {

    const val MOD_ID = "ducky-updater-rework"

    val logger: Logger = LogManager.getLogger()
    val modVersion: String = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata.version.friendlyString
        ?: DateTimeFormatter.ofPattern("yyyy.M").format(LocalDateTime.now())
    val rootModsDir: Path = System.getProperty(SystemProperties.MODS_FOLDER)?.let { Paths.get(it) }
        ?: FabricLoader.getInstance().gameDir.resolve("mods")

    override fun onInitialize() {
        logger.info("Loading common-side DURW")

        ConfigManager
        Database

        CoroutineScope(Dispatchers.IO).launch {
            FileActions.run()
        }

        logger.info("-----[ Custom mods folders ]-----")
        System.getProperty(SystemProperties.ADD_MODS)?.let {
            it.split(File.separator).forEach { pathString ->
                if (pathString.isBlank()) return@forEach

                if (!pathString.startsWith("@")) {
                    val path = LoaderUtil.normalizePath(Paths.get(pathString))
                    logger.info(path)
                }
                logger.info("")
            }
        }

        FabricLoader.getInstance().allMods.forEach { container ->
            if (container.containingMod.isEmpty && container.origin.kind == ModOrigin.Kind.PATH) {
                val path: Path? = container.origin.paths.stream().filter { path ->
                    path.toString().lowercase().endsWith(".jar")
                }.findFirst().getOrNull()
                if (!container.metadata.name.lowercase().contains("fabric"))
                    logger.info("${container.metadata.name}: $path")
            }
        }
    }
}