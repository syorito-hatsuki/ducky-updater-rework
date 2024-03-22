package dev.syoritohatsuki.duckyupdaterrework.util

import com.google.common.collect.ArrayListMultimap
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.dao.AdditionalInfo

val blacklist = setOf("Fabric API")

private const val BOLD = "\u001B[1m"
private const val BRIGHT_GRAY = "\u001B[37m"
private const val BRIGHT_GREEN = "\u001B[92m"
private const val BRIGHT_RED = "\u001B[91m"
private const val GRAY = "\u001B[90m"
private const val RESET = "\u001B[0m"
private const val YELLOW = "\u001B[33m"

fun printModsTree(modsIds: ArrayListMultimap<String, String>, additionalInfos: Map<String, AdditionalInfo>) {
    findRoots(modsIds).sortedBy { additionalInfos[it]?.name }.forEach { root ->
        printProjectTree(root, modsIds, additionalInfos, "", isTail = true, isRoot = true)
    }
}

fun findRoots(projects: ArrayListMultimap<String, String>): Set<String> =
    projects.keys().filterNotNull().toHashSet() - projects.values().filterNotNull().toHashSet()

fun printProjectTree(
    project: String,
    projects: ArrayListMultimap<String, String>,
    additionalInfos: Map<String, AdditionalInfo>,
    prefix: String,
    isTail: Boolean,
    isRoot: Boolean = false,
) {
    val dependencies = projects.get(project)
    val rootSymbol = if (isRoot) " - " else if (isTail) " \\-- " else " |-- "
    val additionalInfo = additionalInfos[project]
    val version = additionalInfo?.version
    DuckyUpdaterReWork.logger.warn(
        "$prefix$rootSymbol${additionalInfo?.name ?: ""} $GRAY[${
            if (!version?.currentVersion.isNullOrBlank()) "$BRIGHT_GRAY${version?.matched}$BRIGHT_RED${version?.currentUnMatch}$GRAY -> " else ""
        }$BRIGHT_GRAY${version?.matched}$BRIGHT_GREEN${version?.newUnMatched}$GRAY]$RESET"
    )
    if (dependencies.isNotEmpty()) {
        dependencies.removeIf { it.isNullOrBlank() || blacklist.contains((additionalInfos[it]?.name ?: "")) }
        val newPrefix = prefix + if (isTail) "    " else " |  "
        dependencies.sortedBy { additionalInfo?.name }.forEachIndexed { index, dependency ->
            val newIsTail = index == dependencies.size - 1
            printProjectTree(dependency, projects, additionalInfos, newPrefix, newIsTail)
        }
    }
}