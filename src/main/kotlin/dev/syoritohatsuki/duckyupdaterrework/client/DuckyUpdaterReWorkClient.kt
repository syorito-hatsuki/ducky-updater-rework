package dev.syoritohatsuki.duckyupdaterrework.client

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.core.command.UpdateCommand
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import dev.syoritohatsuki.duckyupdaterrework.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.util.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object DuckyUpdaterReWorkClient : ClientModInitializer {
    override fun onInitializeClient() {

        logger.info("Loading client-side DURW")

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register {
                rootLiteral("durw-client") {
                    literal("update") {
                        argument("modsIds", ModsIdsArgumentType.modsIds()) {
                            executes(UpdateCommand::update)
                        }
                        literal("all") {
                            executes(UpdateCommand::updateAll)
                        }
                    }
                    literal("check") {
                        executes {
                            val modsIds = Database.modsIds()
                            val additionalInfos = Database.additionalInfoByModsIds(modsIds)
                            buildModsTree(modsIds, additionalInfos).forEach { printer ->
                                val text = Text.empty().apply {
                                    append(Text.literal(printer.prefix))
                                    append(Text.literal("[").formatted(Formatting.DARK_GRAY))
                                    if (printer.matchedVersion.isNotBlank()) {
                                        append(Text.literal(printer.matchedVersion).formatted(Formatting.GRAY))
                                        append(Text.literal(printer.currentUnMatchVersion).formatted(Formatting.RED))
                                        append(Text.literal(" -> ").formatted(Formatting.DARK_GRAY))
                                    }
                                    append(Text.literal(printer.matchedVersion).formatted(Formatting.GRAY))
                                    append(Text.literal(printer.newUnMatchVersion).formatted(Formatting.GREEN))
                                    append(Text.literal("]").formatted(Formatting.DARK_GRAY))
                                    styled { style ->
                                        style.withClickEvent(
                                            ClickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/durw-client update ${printer.modId}"
                                            )
                                        ).withHoverEvent(
                                            HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT, Text.literal(
                                                    additionalInfos[printer.modId]?.changeLog ?: return@styled style
                                                )
                                            )
                                        )
                                    }
                                }

                                it.source.sendFeedback(text)
                            }
                            1
                        }
                    }
                }
            }
        }
    }
}