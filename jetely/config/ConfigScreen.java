package me.cutebow.jetely.config;

import me.cutebow.jetely.util.AssetScanner;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import java.util.List;

public final class ConfigScreen {
    public static Screen open(Screen parent) {
        AssetScanner.ensureUserModelsDir();
        AssetScanner.rescan(MinecraftClient.getInstance().getResourceManager());

        Config cfg = Config.get();
        List<String> modelNames = AssetScanner.getModelBases();
        cfg.ensureSelected(modelNames);

        ConfigBuilder b = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("JetEly"))
                .setSavingRunnable(Config::save);

        ConfigEntryBuilder eb = b.entryBuilder();

        ConfigCategory general = b.getOrCreateCategory(Text.literal("General"));
        general.addEntry(eb.startBooleanToggle(Text.literal("Enable Mod"), cfg.general.enabled)
                .setSaveConsumer(v -> cfg.general.enabled = v).build());
        general.addEntry(eb.startBooleanToggle(Text.literal("Apply To Other Players"), cfg.general.applyToOthers)
                .setSaveConsumer(v -> cfg.general.applyToOthers = v).build());
        general.addEntry(eb.startBooleanToggle(Text.literal("Chase Camera Enabled"), cfg.general.useChaseCamera)
                .setSaveConsumer(v -> cfg.general.useChaseCamera = v).build());
        general.addEntry(eb.startBooleanToggle(Text.literal("Disable Third-Person Collision"), cfg.general.disableThirdPersonClip)
                .setSaveConsumer(v -> cfg.general.disableThirdPersonClip = v).build());

        ConfigCategory ely = b.getOrCreateCategory(Text.literal("Elytra"));
        ely.addEntry(eb.startBooleanToggle(Text.literal("Hide Player Model While Flying"), cfg.elytra.hidePlayerModelWhileFlying)
                .setSaveConsumer(v -> cfg.elytra.hidePlayerModelWhileFlying = v).build());
        ely.addEntry(eb.startBooleanToggle(Text.literal("Randomize Other Players' Models"), cfg.elytra.randomizeOthers)
                .setSaveConsumer(v -> cfg.elytra.randomizeOthers = v).build());
        ely.addEntry(eb.startStringDropdownMenu(Text.literal("Model"), cfg.elytra.selectedModel, s -> Text.literal(s))
                .setSelections(modelNames)
                .setSaveConsumer(v -> cfg.elytra.selectedModel = v).build());

        var eSet = cfg.elytra.getPerModel(cfg.elytra.selectedModel);
        ely.addEntry(eb.startFloatField(Text.literal("Scale"), eSet.scale).setSaveConsumer(v -> eSet.scale = v).build());
        ely.addEntry(eb.startFloatField(Text.literal("Offset X"), eSet.offset[0]).setSaveConsumer(v -> eSet.offset[0] = v).build());
        ely.addEntry(eb.startFloatField(Text.literal("Offset Y"), eSet.offset[1]).setSaveConsumer(v -> eSet.offset[1] = v).build());
        ely.addEntry(eb.startFloatField(Text.literal("Offset Z"), eSet.offset[2]).setSaveConsumer(v -> eSet.offset[2] = v).build());
        ely.addEntry(eb.startFloatField(Text.literal("Yaw"), eSet.rotation[1]).setSaveConsumer(v -> eSet.rotation[1] = v).build());
        ely.addEntry(eb.startFloatField(Text.literal("Camera Distance"), eSet.cameraDistance).setSaveConsumer(v -> eSet.cameraDistance = v).build());
        ely.addEntry(eb.startFloatField(Text.literal("Camera Height"), eSet.cameraHeight).setSaveConsumer(v -> eSet.cameraHeight = v).build());

        ConfigCategory tnt = b.getOrCreateCategory(Text.literal("TNT Minecart"));
        tnt.addEntry(eb.startBooleanToggle(Text.literal("Enable Replacement"), cfg.tnt.enabled)
                .setSaveConsumer(v -> cfg.tnt.enabled = v).build());
        tnt.addEntry(eb.startBooleanToggle(Text.literal("Hide Vanilla TNT Minecart"), cfg.tnt.hideVanilla)
                .setSaveConsumer(v -> cfg.tnt.hideVanilla = v).build());
        tnt.addEntry(eb.startStringDropdownMenu(Text.literal("Model"), cfg.tnt.selectedModel, s -> Text.literal(s))
                .setSelections(modelNames)
                .setSaveConsumer(v -> cfg.tnt.selectedModel = v).build());

        var tSet = cfg.tnt.getPerModel(cfg.tnt.selectedModel);
        tnt.addEntry(eb.startFloatField(Text.literal("Scale"), tSet.scale).setSaveConsumer(v -> tSet.scale = v).build());
        tnt.addEntry(eb.startFloatField(Text.literal("Offset X"), tSet.offset[0]).setSaveConsumer(v -> tSet.offset[0] = v).build());
        tnt.addEntry(eb.startFloatField(Text.literal("Offset Y"), tSet.offset[1]).setSaveConsumer(v -> tSet.offset[1] = v).build());
        tnt.addEntry(eb.startFloatField(Text.literal("Offset Z"), tSet.offset[2]).setSaveConsumer(v -> tSet.offset[2] = v).build());
        tnt.addEntry(eb.startFloatField(Text.literal("Yaw"), tSet.rotation[1]).setSaveConsumer(v -> tSet.rotation[1] = v).build());

        ConfigCategory perf = b.getOrCreateCategory(Text.literal("Preformance"));
        perf.addEntry(eb.startBooleanToggle(Text.literal("Frustum Culling"), cfg.perf.frustumCulling)
                .setSaveConsumer(v -> cfg.perf.frustumCulling = v).build());
        perf.addEntry(eb.startBooleanToggle(Text.literal("Distance Culling"), cfg.perf.distanceCulling)
                .setSaveConsumer(v -> cfg.perf.distanceCulling = v).build());
        perf.addEntry(eb.startFloatField(Text.literal("Max Distance"), cfg.perf.maxDistance)
                .setSaveConsumer(v -> cfg.perf.maxDistance = v).build());
        perf.addEntry(eb.startBooleanToggle(Text.literal("Batch By Texture"), cfg.perf.batchByTexture)
                .setSaveConsumer(v -> cfg.perf.batchByTexture = v).build());
        perf.addEntry(eb.startIntField(Text.literal("Tick Decimation"), cfg.perf.tickDecimation)
                .setMin(1).setSaveConsumer(v -> cfg.perf.tickDecimation = v).build());

        ConfigCategory modelsCat = b.getOrCreateCategory(Text.literal("Models"));
        modelsCat.addEntry(
                eb.startTextDescription(
                        Text.literal("Open Models Folder").styled(s -> s
                                .withUnderline(true)
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.OPEN_FILE,
                                        AssetScanner.getUserModelsDir().toAbsolutePath().toString()
                                ))
                        )
                ).build()
        );

        ConfigCategory cfgCat = b.getOrCreateCategory(Text.literal("Config"));
        cfgCat.addEntry(
                eb.startTextDescription(
                        Text.literal("Open Config Folder").styled(s -> s
                                .withUnderline(true)
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.OPEN_FILE,
                                        FabricLoader.getInstance().getConfigDir().toAbsolutePath().toString()
                                ))
                        )
                ).build()
        );

        ConfigCategory bombing = b.getOrCreateCategory(Text.literal("Bombing"));
        bombing.addEntry(eb.startBooleanToggle(Text.literal("Enable Bombing"), cfg.bombing.enabled)
                .setSaveConsumer(v -> cfg.bombing.enabled = v).build());
        bombing.addEntry(eb.startIntField(Text.literal("Bomb Cooldown (ms)"), cfg.bombing.bombCooldownMs)
                .setSaveConsumer(v -> cfg.bombing.bombCooldownMs = Math.max(0, v)).build());
        bombing.addEntry(eb.startFloatField(Text.literal("Offset X"), cfg.bombing.offsetX).setSaveConsumer(v -> cfg.bombing.offsetX = v).build());
        bombing.addEntry(eb.startFloatField(Text.literal("Offset Y"), cfg.bombing.offsetY).setSaveConsumer(v -> cfg.bombing.offsetY = v).build());
        bombing.addEntry(eb.startFloatField(Text.literal("Offset Z"), cfg.bombing.offsetZ).setSaveConsumer(v -> cfg.bombing.offsetZ = v).build());
        bombing.addEntry(eb.startFloatField(Text.literal("Yaw Offset"), cfg.bombing.yawOffset).setSaveConsumer(v -> cfg.bombing.yawOffset = v).build());
        bombing.addEntry(eb.startFloatField(Text.literal("Drop Speed"), (float) cfg.bombing.dropSpeed).setSaveConsumer(v -> cfg.bombing.dropSpeed = v).build());

        return b.build();
    }
}
