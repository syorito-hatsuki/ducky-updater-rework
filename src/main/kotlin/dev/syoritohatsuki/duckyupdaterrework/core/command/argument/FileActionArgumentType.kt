package dev.syoritohatsuki.duckyupdaterrework.core.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.syoritohatsuki.duckyupdaterrework.core.util.Downloader
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

class FileActionArgumentType : ArgumentType<Downloader.FileAction> {

    companion object {
        fun fileAction(): FileActionArgumentType = FileActionArgumentType()

        fun getFileAction(context: CommandContext<out CommandSource>, argumentName: String): Downloader.FileAction =
            context.getArgument(argumentName, Downloader.FileAction::class.java)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>, builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> =
        CommandSource.suggestMatching(Downloader.FileAction.values().map { it.name }, builder) ?: Suggestions.empty()

    override fun parse(reader: StringReader): Downloader.FileAction {
        val cursor = reader.cursor

        while (reader.canRead()) reader.skip()

        return Downloader.FileAction.valueOf(reader.string.substring(cursor, reader.cursor))
    }

    override fun getExamples(): Collection<String> = Downloader.FileAction.values().map { it.name }
}