package dev.syoritohatsuki.duckyupdaterrework.core.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.syoritohatsuki.duckyupdaterrework.core.util.FileActions
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

class FileActionArgumentType : ArgumentType<FileActions.FileAction> {

    companion object {
        fun fileAction(): FileActionArgumentType = FileActionArgumentType()

        fun getFileAction(context: CommandContext<out CommandSource>, argumentName: String): FileActions.FileAction =
            context.getArgument(argumentName, FileActions.FileAction::class.java)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>, builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> =
        CommandSource.suggestMatching(FileActions.FileAction.values().map { it.name }, builder) ?: Suggestions.empty()

    override fun parse(reader: StringReader): FileActions.FileAction {
        val cursor = reader.cursor

        while (reader.canRead()) reader.skip()

        return FileActions.FileAction.valueOf(reader.string.substring(cursor, reader.cursor))
    }

    override fun getExamples(): Collection<String> = FileActions.FileAction.values().map { it.name }
}