package dev.syoritohatsuki.duckyupdaterrework.util

/*  ASCII colors  */
const val BOLD = "\u001B[1m"
const val BRIGHT_GRAY = "\u001B[37m"
const val BRIGHT_GREEN = "\u001B[92m"
const val BRIGHT_RED = "\u001B[91m"
const val GRAY = "\u001B[90m"
const val RESET = "\u001B[0m"
const val YELLOW = "\u001B[33m"

/*  Log4j line templates  */
const val UPDATE_AVAILABLE = "$BOLD${YELLOW}Updates available$RESET"
const val PROJECT_UPDATE = "{} $GRAY[$BRIGHT_GRAY{}$BRIGHT_RED{}$GRAY -> $BRIGHT_GRAY{}$BRIGHT_GREEN{}$GRAY]$RESET"
const val PROJECT_NEW = "{} $GRAY[$BRIGHT_GREEN{}$GRAY]$RESET"
const val UPDATE_FAILED = "$BOLD${BRIGHT_RED}Can't update {}, please check logs!$RESET"
const val UPDATE_ON_START_DISABLED = "$BOLD${BRIGHT_RED}Update on startup disabled$RESET"
const val UPDATE_ON_START_ENABLED = "$BOLD${BRIGHT_GREEN}Update on startup enabled$RESET"
const val SOMETHING_WENT_WRONG = "$BOLD${BRIGHT_RED}Something went wrong :($RESET"
const val IGNORE_UPDATE = "$BOLD$BRIGHT_GREEN{} update {} added to ignore$RESET"
const val ALL_UP_TO_DATE = "$BOLD${BRIGHT_GREEN}All mods up-to-date$RESET"
const val NOTHING_TO_IGNORE = "$BOLD${BRIGHT_RED}Update not found for ignoring$RESET"