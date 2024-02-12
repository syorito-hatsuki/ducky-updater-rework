package dev.syoritohatsuki.duckyupdaterrework.core.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

class ModsIdsArgumentType : ArgumentType<List<String>> {

    private val suggestion: List<String> = FabricLoader.getInstance().allMods.map { it.metadata.id }

    companion object {
        fun modsIds(): ModsIdsArgumentType = ModsIdsArgumentType()

        fun getModsIds(context: CommandContext<ServerCommandSource>, argumentName: String): List<String> {
            return context.getArgument(argumentName, List::class.java) as List<String>
        }
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>, builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> = when (context.source) {
        is CommandSource -> CommandSource.suggestMatching(suggestion, builder)

        else -> Suggestions.empty()
    }

    override fun parse(reader: StringReader): List<String> {
        val cursor = reader.cursor

        while (reader.canRead()) reader.skip()

        val mods = reader.string.substring(cursor, reader.cursor).removePrefix("[").removeSuffix("]")

        return mods.split(", ")
    }

    override fun getExamples(): Collection<String> = listOf(DuckyUpdaterReWork.MOD_ID)
}