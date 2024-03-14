package dev.syoritohatsuki.duckyupdaterrework.util

import com.google.common.collect.ArrayListMultimap
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork

val blacklist = setOf("Fabric API")

fun printModsTree(modsIds: ArrayListMultimap<String, String>) {
    findRoots(modsIds).sorted().forEach { root ->
        printProjectTree(root, modsIds, "", isTail = true, isRoot = true, blacklist)
    }
}

fun findRoots(projects: ArrayListMultimap<String, String>): Set<String> =
    projects.keys().toHashSet() - projects.values().toHashSet()

fun printProjectTree(
    project: String,
    projects: ArrayListMultimap<String, String>,
    prefix: String,
    isTail: Boolean,
    isRoot: Boolean = false,
    blacklist: Set<String> = emptySet()
) {
    val dependencies = projects.get(project)
    val rootSymbol = if (isRoot) " - " else if (isTail) " \\-- " else " |-- "
    DuckyUpdaterReWork.logger.warn("$prefix$rootSymbol$project")
    if (dependencies.isNotEmpty()) {
        val newPrefix = prefix + if (isTail) "  " else " |  "
        for ((index, dependency) in dependencies.sorted().withIndex()) {
            val newIsTail = index == dependencies.size - 1
            if (!blacklist.contains(dependency)) {
                printProjectTree(dependency, projects, newPrefix, newIsTail, blacklist = blacklist)
            }
        }
    }
}