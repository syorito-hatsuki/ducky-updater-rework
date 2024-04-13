package dev.syoritohatsuki.duckyupdaterrework.core.util

import com.google.common.collect.ArrayListMultimap
import dev.syoritohatsuki.duckyupdaterrework.core.dto.durw.Printer
import dev.syoritohatsuki.duckyupdaterrework.core.dto.modrinth.AdditionalInfo

val blacklist = setOf("Fabric API")

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
    val rootSymbol = if (isRoot) " - " else if (isTail) " \\-- " else " |-- "
    val additionalInfo = additionalInfos[project]
    val version = additionalInfo?.version

    buffer.add(
        Printer(
            project,
            "$prefix$rootSymbol${additionalInfo?.name ?: ""}",
            !version?.currentVersion.isNullOrBlank(),
            version?.matched ?: "",
            version?.currentUnMatch ?: "",
            version?.newUnMatched ?: ""
        )
    )

    projects.get(project).takeIf { it.isNotEmpty() }?.apply {
        removeIf { it.isNullOrBlank() || blacklist.contains((additionalInfos[it]?.name ?: "")) }
        sortedBy { additionalInfo?.name }.forEachIndexed { index, dependency ->
            buildProjectTree(
                dependency,
                projects,
                additionalInfos,
                prefix + if (isTail) "    " else " |  ",
                index == size - 1,
                buffer = buffer
            )
        }
    }
}