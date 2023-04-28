package dev.syoritohatsuki.duckyupdater.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.util.AnsiColorsKt;
import dev.syoritohatsuki.duckyupdater.util.Util;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    private final Logger logger = DuckyUpdater.INSTANCE.getLogger();

    @Inject(method = "loadWorld", at = @At("TAIL"))
    private void checkUpdates(CallbackInfo ci) {

        AtomicBoolean firstLine = new AtomicBoolean(true);
        HashMap<String, ModContainer> hashes = new HashMap<>();

        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            String sha512Hash = Util.INSTANCE.getSha512Hash(modContainer);
            if (sha512Hash != null) hashes.put(sha512Hash, modContainer);
        });

        JsonParser.parseString(DuckyUpdater.INSTANCE.getUpdates(hashes.keySet())).getAsJsonObject().asMap().forEach((hash, jsonElement) -> {
            JsonObject data = jsonElement.getAsJsonObject();
            ModMetadata metadata = hashes.get(hash).getMetadata();
            String name = metadata.getName();

            String oldVersion = metadata.getVersion().getFriendlyString();
            String newVersion = data.get("version_number").getAsString();

            if (firstLine.get()) {
                logger.info("");
                logger.info(AnsiColorsKt.BOLD + AnsiColorsKt.YELLOW + "Updates available" + AnsiColorsKt.RESET);
                firstLine.set(false);
            }

            final String match = Util.INSTANCE.match(oldVersion.toCharArray(), newVersion.toCharArray());

            if (match == null) return;

            oldVersion = oldVersion.replace(match, "");
            newVersion = newVersion.replace(match, "");

            logger.info("\t- {} " + AnsiColorsKt.GRAY + "[" + AnsiColorsKt.BRIGHT_GRAY + "{}" + AnsiColorsKt.BRIGHT_RED + "{}" + AnsiColorsKt.GRAY + " -> " + AnsiColorsKt.BRIGHT_GRAY + "{}" + AnsiColorsKt.BRIGHT_GREEN + "{}" + AnsiColorsKt.GRAY + "]" + AnsiColorsKt.RESET, name, match, oldVersion, match, newVersion);

        });
        if (!firstLine.get()) logger.info("");
    }
}
