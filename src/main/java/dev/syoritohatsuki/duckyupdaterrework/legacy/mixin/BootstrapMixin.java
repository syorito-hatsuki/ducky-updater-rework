package dev.syoritohatsuki.duckyupdaterrework.legacy.mixin;

import dev.syoritohatsuki.duckyupdaterrework.legacy.DuckyUpdaterReWork;
import dev.syoritohatsuki.duckyupdaterrework.legacy.util.ConfigManager;
import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public abstract class BootstrapMixin {
    @Inject(method = "initialize", at = @At("HEAD"))
    private static void requestUpdates(CallbackInfo ci) {
        new Thread(() -> {
            DuckyUpdaterReWork.INSTANCE.checkForUpdate();
            if (ConfigManager.INSTANCE.isUpdateOnStartUpEnabled()) {
                DuckyUpdaterReWork.INSTANCE.updateAll(null);
            }
        }).start();
    }
}