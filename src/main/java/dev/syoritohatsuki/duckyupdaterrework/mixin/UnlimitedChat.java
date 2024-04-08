package dev.syoritohatsuki.duckyupdaterrework.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatHud.class)
public abstract class UnlimitedChat {
    @Inject(method = "getWidth()I", at = @At("HEAD"), cancellable = true)
    private void unlimitedChatWidth(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(1000);
    }

    @Inject(method = "getHeight()I", at = @At("HEAD"), cancellable = true)
    private void unlimitedChatHeight(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(1000);
    }
}
