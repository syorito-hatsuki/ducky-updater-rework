package dev.syoritohatsuki.duckyupdaterrework.server.message

import dev.syoritohatsuki.duckyupdaterrework.core.dto.durw.Printer
import org.apache.logging.log4j.Logger

fun Logger.updateAvailable(printer: Printer) = info(
    "${printer.prefix} $GRAY[${
        if (printer.currentExist) "$BRIGHT_GRAY${printer.matchedVersion}$BRIGHT_RED${printer.currentUnMatchVersion}$GRAY -> "
        else ""
    }$BRIGHT_GRAY${printer.matchedVersion}$BRIGHT_GREEN${printer.newUnMatchVersion}$GRAY]$RESET"
)