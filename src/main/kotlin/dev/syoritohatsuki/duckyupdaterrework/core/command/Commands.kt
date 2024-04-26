package dev.syoritohatsuki.duckyupdaterrework.core.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.syoritohatsuki.duckyupdaterrework.core.command.execute.check
import dev.syoritohatsuki.duckyupdaterrework.core.command.execute.update
import dev.syoritohatsuki.duckyupdaterrework.core.command.execute.updateAll
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.argument
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.literal
import net.minecraft.command.CommandSource

fun LiteralArgumentBuilder<out CommandSource>.commands() {
    literal("update") {
        argument("modsIds", dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType.modsIds()) {
            executes(::update)
        }
        argument("modsIdsString", com.mojang.brigadier.arguments.StringArgumentType.string()) {
            executes(::update)
        }
        literal("all") {
            executes(::updateAll)
        }
    }
    literal("check") {
        executes(::check)
    }
}