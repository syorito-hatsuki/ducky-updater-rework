package dev.syoritohatsuki.duckyupdater.util

private const val BOLD = "\u001B[1m"
private const val BRIGHT_GRAY = "\u001B[37m"
private const val BRIGHT_GREEN = "\u001B[92m"
private const val BRIGHT_RED = "\u001B[91m"
private const val GRAY = "\u001B[90m"
private const val RESET = "\u001B[0m"
private const val YELLOW = "\u001B[33m"

const val UPDATE_AVAILABLE = "$BOLD${YELLOW}Updates available$RESET"
const val MOD_UPDATE = "\t- {} $GRAY[$BRIGHT_GRAY{}$BRIGHT_RED{}$GRAY -> $BRIGHT_GRAY{}$BRIGHT_GREEN{}$GRAY]$RESET"