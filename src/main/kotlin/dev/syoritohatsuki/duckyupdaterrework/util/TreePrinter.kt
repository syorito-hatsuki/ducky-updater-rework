package dev.syoritohatsuki.duckyupdaterrework.util

import com.google.common.collect.ArrayListMultimap
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.dto.AdditionalInfo

val blacklist = setOf("Fabric API")

data class Printer(
    val projectId: String,
    val prefix: String,
    val matchedVersion: String,
    val currentUnMatchVersion: String,
    val newUnMatchVersion: String,
)

fun buildModsTree(
    modsIds: ArrayListMultimap<String, String>, additionalInfos: Map<String, AdditionalInfo>
): List<Printer> = mutableListOf<Printer>().apply {
    findRoots(modsIds).sortedBy { additionalInfos[it]?.name }.forEach { root ->
        buildProjectTree(root, modsIds, additionalInfos, "", isTail = true, isRoot = true, this)
    }
}

fun findRoots(projects: ArrayListMultimap<String, String>): Set<String> =
    projects.keys().filterNotNull().toHashSet() - projects.values().filterNotNull().toHashSet()

fun buildProjectTree(
    project: String,
    projects: ArrayListMultimap<String, String>,
    additionalInfos: Map<String, AdditionalInfo>,
    prefix: String,
    isTail: Boolean,
    isRoot: Boolean = false,
    buffer: MutableList<Printer>,
) {
    val dependencies = projects.get(project)
    val rootSymbol = if (isRoot) " - " else if (isTail) " \\-- " else " |-- "
    val additionalInfo = additionalInfos[project]
    val version = additionalInfo?.version

    DuckyUpdaterReWork.logger.error(isRoot)

    buffer.add(
        Printer(
            project,
            "$prefix$rootSymbol${additionalInfo?.name ?: ""}",
            version?.matched ?: "",
            version?.currentUnMatch ?: "",
            version?.newUnMatched ?: ""
        )
    )

    if (dependencies.isNotEmpty()) {
        dependencies.removeIf { it.isNullOrBlank() || blacklist.contains((additionalInfos[it]?.name ?: "")) }
        val newPrefix = prefix + if (isTail) "    " else " |  "
        dependencies.sortedBy { additionalInfo?.name }.forEachIndexed { index, dependency ->
            val newIsTail = index == dependencies.size - 1
            buildProjectTree(dependency, projects, additionalInfos, newPrefix, newIsTail, buffer = buffer)
        }
    }
}