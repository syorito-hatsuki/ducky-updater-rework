package dev.syoritohatsuki.duckyupdater.mixin;

import com.google.gson.JsonObject;
import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.dto.VersionDiff;
import dev.syoritohatsuki.duckyupdater.util.AnsiKt;
import dev.syoritohatsuki.duckyupdater.util.UtilKt;
import net.fabricmc.loader.api.metadata.ModMetadata;
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

        instance.getUpdates().forEach((hash, jsonElement) -> {
            JsonObject data = jsonElement.getAsJsonObject();
            ModMetadata metadata = instance.getHashes().get(hash).getMetadata();

            if (firstLine.get()) {
                logger.info("");
                logger.info(AnsiKt.UPDATE_AVAILABLE);
                firstLine.set(false);
            }

            VersionDiff version = UtilKt.diff(metadata.getVersion().getFriendlyString(), data.get("version_number").getAsString());
            if (version == null) return;

            logger.info(AnsiKt.MOD_UPDATE, metadata.getName(), version.getMatched(), version.getOldVersion(), version.getMatched(), version.getNewVersion());

        });
        if (!firstLine.get()) logger.info("");
    }
}
