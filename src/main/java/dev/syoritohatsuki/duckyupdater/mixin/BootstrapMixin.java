package dev.syoritohatsuki.duckyupdater.mixin;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.util.ConfigManager;
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
            DuckyUpdater.INSTANCE.checkForUpdate();
            if (ConfigManager.INSTANCE.isUpdateOnStartUpEnabled()) {
                DuckyUpdater.INSTANCE.updateAll(null);
            }
        }).start();
    }
}