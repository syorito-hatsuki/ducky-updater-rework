package dev.syoritohatsuki.duckyupdaterrework.core.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

class ModsIdsArgumentType : ArgumentType<List<String>> {

    companion object {
        fun modsIds(): ModsIdsArgumentType = ModsIdsArgumentType()

        @Suppress("UNCHECKED_CAST")
        fun getModsIds(context: CommandContext<out CommandSource>, argumentName: String): List<String> =
            context.getArgument(argumentName, List::class.java) as List<String>
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>, builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> =
        CommandSource.suggestMatching(DuckyUpdaterReWork.modsIds, builder) ?: Suggestions.empty()

    override fun parse(reader: StringReader): List<String> {
        val cursor = reader.cursor

        while (reader.canRead()) reader.skip()

        return reader.string.substring(cursor, reader.cursor).split(" ")
    }

    override fun getExamples(): Collection<String> = listOf(DuckyUpdaterReWork.MOD_ID)
}