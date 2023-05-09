package dev.syoritohatsuki.duckyupdater.util

import dev.syoritohatsuki.duckyupdater.DuckyUpdater
import dev.syoritohatsuki.duckyupdater.DuckyUpdater.logger
import java.util.concurrent.atomic.AtomicBoolean

private const val BOLD = "\u001B[1m"
private const val BRIGHT_GRAY = "\u001B[37m"
private const val BRIGHT_GREEN = "\u001B[92m"
private const val BRIGHT_RED = "\u001B[91m"
private const val GRAY = "\u001B[90m"
private const val RESET = "\u001B[0m"
private const val YELLOW = "\u001B[33m"

private const val UPDATE_AVAILABLE = "$BOLD${YELLOW}Updates available$RESET"
private const val MOD_UPDATE =
    "\t- {} $GRAY[$BRIGHT_GRAY{}$BRIGHT_RED{}$GRAY -> $BRIGHT_GRAY{}$BRIGHT_GREEN{}$GRAY]$RESET"
private const val UPDATE_SUCCESS = "$BOLD$BRIGHT_GREEN{} successful updated$RESET"
private const val UPDATE_FAILED = "$BOLD${BRIGHT_RED}Can't update {}, please check logs!$RESET"
private const val UPDATE_ON_START_DISABLED = "$BOLD${BRIGHT_RED}Update on startup disabled$RESET"
private const val UPDATE_ON_START_ENABLED = "$BOLD${BRIGHT_GREEN}Update on startup enabled$RESET"
private const val SOMETHING_WENT_WRONG = "$BOLD${BRIGHT_RED}Something went wrong :($RESET"
private const val IGNORE_UPDATE = "$BOLD$BRIGHT_GREEN{} update {} added to ignore$RESET"

fun DuckyUpdater.updateListCliMessage() {
    val firstLine = AtomicBoolean(true)

    updateVersions.forEach { (_, modName, _, _, _, _, versions) ->
        if (firstLine.get()) {
            logger.info("")
            logger.info(UPDATE_AVAILABLE)
            firstLine.set(false)
        }
        logger.info(
            MOD_UPDATE,
            modName,
            versions.matched,
            versions.oldVersion,
            versions.matched,
            versions.newVersion
        )
    }

    if (!firstLine.get()) logger.info("")
}

fun updateStatusCliMessage(modId: String, status: Int) = when (status) {
    0 -> logger.info(UPDATE_FAILED, modId)
    1 -> logger.info(UPDATE_SUCCESS, modId)
    else -> logger.info(SOMETHING_WENT_WRONG)
}

fun enableUpdateOnStartUpCliMessage(enable: Boolean) = when (enable) {
    true -> logger.info(UPDATE_ON_START_DISABLED)
    false -> logger.info(UPDATE_ON_START_ENABLED)
}

fun somethingWentWrongCliMessage() = logger.info(SOMETHING_WENT_WRONG)

fun ignoreUpdateCliMessage(modId: String, version: String) = logger.info(IGNORE_UPDATE, modId, version)