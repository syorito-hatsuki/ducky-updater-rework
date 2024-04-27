package dev.syoritohatsuki.duckyupdaterrework.core.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.syoritohatsuki.duckyupdaterrework.core.util.Downloader
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

class DownloadModeArgumentType : ArgumentType<Downloader.Mode> {

    companion object {
        fun downloadType(): DownloadModeArgumentType = DownloadModeArgumentType()

        fun getDownloadType(context: CommandContext<out CommandSource>, argumentName: String): Downloader.Mode =
            context.getArgument(argumentName, Downloader.Mode::class.java)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>, builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> =
        CommandSource.suggestMatching(Downloader.Mode.values().map { it.name }, builder) ?: Suggestions.empty()

    override fun parse(reader: StringReader): Downloader.Mode {
        val cursor = reader.cursor

        while (reader.canRead()) reader.skip()

        return Downloader.Mode.valueOf(reader.string.substring(cursor, reader.cursor))
    }

    override fun getExamples(): Collection<String> = Downloader.Mode.values().map { it.name }
}