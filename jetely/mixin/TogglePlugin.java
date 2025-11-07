package me.cutebow.jetely.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public final class TogglePlugin implements IMixinConfigPlugin {
    private static Boolean enabled;

    private static boolean isEnabled() {
        if (enabled != null) return enabled;
        try {
            Path p = FabricLoader.getInstance().getConfigDir().resolve("jetely.json");
            if (Files.exists(p)) {
                String s = Files.readString(p);
                JsonObject root = JsonParser.parseString(s).getAsJsonObject();
                JsonObject general = root.has("general") && root.get("general").isJsonObject() ? root.getAsJsonObject("general") : null;
                enabled = general != null && (!general.has("enabled") || general.get("enabled").getAsBoolean());
            } else {
                enabled = true;
            }
        } catch (Exception e) {
            enabled = true;
        }
        return enabled;
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return isEnabled(); }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
