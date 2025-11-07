package me.cutebow.jetely.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Config {
    public static final class General {
        public boolean enabled = true;
        public boolean applyToOthers = true;
        public boolean useChaseCamera = true;
        public boolean disableThirdPersonClip = false;
    }

    public static final class ModelSettings {
        public float scale = 10f;
        public float[] offset = new float[]{0f, 0f, 0f};
        public float[] rotation = new float[]{0f, 0f, 0f};
        public float cameraDistance = 0f;
        public float cameraHeight = 1f;
    }

    public static final class ElytraGroup {
        public String selectedModel = "mcjet";
        public boolean hidePlayerModelWhileFlying = true;
        public boolean randomizeOthers = false;
        public Map<String, ModelSettings> perModel = new HashMap<>();
        public ModelSettings getPerModel(String base) { return perModel.computeIfAbsent(base == null ? "" : base, k -> new ModelSettings()); }
    }

    public static final class TntGroup {
        public boolean enabled = true;
        public boolean hideVanilla = true;
        public String selectedModel = "fatmanbomb";
        public Map<String, ModelSettings> perModel = new HashMap<>();
        public ModelSettings getPerModel(String base) { return perModel.computeIfAbsent(base == null ? "" : base, k -> new ModelSettings()); }
    }

    public static final class Perf {
        public boolean frustumCulling = true;
        public boolean distanceCulling = false;
        public float maxDistance = 96f;
        public boolean batchByTexture = true;
        public int tickDecimation = 1;
    }

    public static final class Bombing {
        public boolean enabled = true;
        public int bombCooldownMs = 300;
        public float offsetX = 0f;
        public float offsetY = -1f;
        public float offsetZ = 0f;
        public float yawOffset = 0f;
        public double dropSpeed = 1.0;
    }

    public General general = new General();
    public ElytraGroup elytra = new ElytraGroup();
    public TntGroup tnt = new TntGroup();
    public Perf perf = new Perf();
    public Map<String, String> assigned = new HashMap<>();
    public Bombing bombing = new Bombing();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("jetely.json");
    private static Config INSTANCE;

    public Config() {
        putElytra("atomic_bomb_mat_0", 3f, 0f, 0f, 0f, 0f, 180f, 0f, -3f, 1.5f);
        putElytra("bonedragon", 0.3f, 0f, 0f, 0f, 0f, 0f, 0f, -5f, 2f);
        putElytra("f16", 3f, 0f, -0.5f, 0f, 0f, 180f, 0f, -4f, 1.5f);
        putElytra("fatmanbomb", 3f, 0f, 0f, 0f, 0f, 270f, 0f, -3f, 1.5f);
        putElytra("lowpolyjet", 1f, 0f, -1f, -1f, 0f, 0f, 0f, -3f, 1.5f);
        putElytra("mcjet", 1f, 0f, -1.6f, 0f, 0f, 0f, 0f, -8f, 3f);
        putElytra("mcste", 0.03f, 0f, -1.3f, -0.6f, 0f, 0f, 0f, -4f, 2f);
        putElytra("pterodacty", 8f, 0.2f, -4f, -1.3f, 0f, 180f, 0f, -3f, 2f);
        putElytra("realcamerascan", 0.3f, 0f, -1f, -1f, 0f, 180f, 0f, -4f, 2f);
        putElytra("realistic", 0.03f, 0f, -1f, 0f, 0f, 0f, 0f, -4f, 1.5f);
        putElytra("starship", 2f, 0f, 0f, 0f, 0f, 180f, 0f, -2f, 2f);
        putElytra("startrek", 0.3f, 0f, -0.5f, -1f, 0f, 180f, 0f, -5f, 2f);
        putElytra("tie", 0.4f, 0f, -4f, -0.8f, 0f, 180f, 0f, -8f, 2f);

        putTnt("lowpolyjet", 3f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 2f);
        putTnt("realcamerascan", 0.5f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 2f);
        putTnt("tie", 0.3f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 2f);
        putTnt("mcjet", 2f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 2f);
        putTnt("mcste", 0.03f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 2f);
        putTnt("f16", 5f, 0f, 3f, 0f, 0f, 0f, 0f, 8f, 2f);
        putTnt("fatmanbomb", 2f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 2f);
        putTnt("atomic_bomb_mat_0", 2f, 0f, 0f, 0f, 0f, 30f, 0f, 8f, 2f);
    }

    private void putElytra(String key, float sc, float ox, float oy, float oz, float rx, float ry, float rz, float cd, float ch) {
        ModelSettings s = new ModelSettings();
        s.scale = sc;
        s.offset = new float[]{ox, oy, oz};
        s.rotation = new float[]{rx, ry, rz};
        s.cameraDistance = cd;
        s.cameraHeight = ch;
        elytra.perModel.put(key, s);
    }

    private void putTnt(String key, float sc, float ox, float oy, float oz, float rx, float ry, float rz, float cd, float ch) {
        ModelSettings s = new ModelSettings();
        s.scale = sc;
        s.offset = new float[]{ox, oy, oz};
        s.rotation = new float[]{rx, ry, rz};
        s.cameraDistance = cd;
        s.cameraHeight = ch;
        tnt.perModel.put(key, s);
    }

    public static Config get() { if (INSTANCE == null) load(); return INSTANCE; }

    public static void load() {
        try {
            if (Files.exists(FILE)) INSTANCE = GSON.fromJson(Files.readString(FILE), Config.class);
            else { INSTANCE = new Config(); save(); }
        } catch (Exception e) { INSTANCE = new Config(); }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(INSTANCE));
        } catch (Exception ignored) {}
    }

    public void ensureSelected(List<String> available) {
        if (available == null || available.isEmpty()) return;
        if (elytra.selectedModel == null || elytra.selectedModel.isEmpty() || !available.contains(elytra.selectedModel))
            elytra.selectedModel = available.get(0);
        if (tnt.selectedModel == null || tnt.selectedModel.isEmpty() || !available.contains(tnt.selectedModel))
            tnt.selectedModel = available.get(0);
    }
}
