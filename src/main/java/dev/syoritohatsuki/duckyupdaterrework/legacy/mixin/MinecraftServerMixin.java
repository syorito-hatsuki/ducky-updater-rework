package dev.syoritohatsuki.duckyupdaterrework.legacy.mixin;

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.syoritohatsuki.duckyupdaterrework.legacy.util.AnsiKt.updateListCliMessage;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "loadWorld", at = @At("TAIL"))
    private void checkUpdates(CallbackInfo ci) {
        updateListCliMessage(DuckyUpdaterReWork.INSTANCE);
    }
}
