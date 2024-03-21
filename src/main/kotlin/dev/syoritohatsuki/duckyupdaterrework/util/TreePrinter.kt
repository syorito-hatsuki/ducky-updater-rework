package dev.syoritohatsuki.duckyupdaterrework.util

import com.google.common.collect.ArrayListMultimap
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork

val blacklist = setOf("Fabric API", "")

fun printModsTree(modsIds: ArrayListMultimap<String, String>) {
    findRoots(modsIds).sorted().forEach { root ->
        printProjectTree(root, modsIds, "", isTail = true, isRoot = true)
    }
}

fun findRoots(projects: ArrayListMultimap<String, String>): Set<String> =
    projects.keys().filterNotNull().toHashSet() - projects.values().filterNotNull().toHashSet()

fun printProjectTree(
    project: String,
    projects: ArrayListMultimap<String, String>,
    prefix: String,
    isTail: Boolean,
    isRoot: Boolean = false,
) {
    val dependencies = projects.get(project)
    val rootSymbol = if (isRoot) " - " else if (isTail) " \\-- " else " |-- "

    DuckyUpdaterReWork.logger.warn("$prefix$rootSymbol$project")
    if (dependencies.removeIf { blacklist.contains(it) } && dependencies.isNotEmpty()) {
        val newPrefix = prefix + if (isTail) "    " else " |  "
        dependencies.sorted().forEachIndexed { index, dependency ->
            val newIsTail = index == dependencies.size - 1
            printProjectTree(dependency, projects, newPrefix, newIsTail)
        }
    }
}