package dev.syoritohatsuki.duckyupdater.mixin;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.util.AnsiKt;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    private final DuckyUpdater instance = DuckyUpdater.INSTANCE;
    private final Logger logger = instance.getLogger();

    @Inject(method = "loadWorld", at = @At("TAIL"))
    private void checkUpdates(CallbackInfo ci) {

        AtomicBoolean firstLine = new AtomicBoolean(true);

        instance.getUpdateVersions().forEach(updateVersions -> {

            if (firstLine.get()) {
                logger.info("");
                logger.info(AnsiKt.UPDATE_AVAILABLE);
                firstLine.set(false);
            }

            logger.info(
                    AnsiKt.MOD_UPDATE,
                    updateVersions.getModName(),
                    updateVersions.getVersions().getMatched(),
                    updateVersions.getVersions().getOldVersion(),
                    updateVersions.getVersions().getMatched(),
                    updateVersions.getVersions().getNewVersion()
            );

        });
        if (!firstLine.get()) logger.info("");
    }
}
