package me.cutebow.jetely.util;

import me.cutebow.jetely.JetElyClient;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class AssetScanner {
    private static final List<String> OBJ_BASES = new ArrayList<>();
    private static final Path USER_MODELS_DIR = FabricLoader.getInstance().getConfigDir().resolve("jetely").resolve("models");

    private AssetScanner() {}

    public static void init() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(reloadListener());
    }

    public static SimpleSynchronousResourceReloadListener reloadListener() {
        return new SimpleSynchronousResourceReloadListener() {
            @Override public Identifier getFabricId() { return Identifier.of(JetElyClient.MODID, "asset_scanner"); }
            @Override public void reload(ResourceManager manager) { rescan(manager); }
        };
    }

    public static void ensureUserModelsDir() {
        try { Files.createDirectories(USER_MODELS_DIR); } catch (Exception ignored) {}
    }

    public static void rescan(ResourceManager manager) {
        OBJ_BASES.clear();

        if (manager != null) {
            Map<Identifier, ?> found = manager.findResources("models/custom",
                    id -> id.getNamespace().equals(JetElyClient.ASSET_NS) && id.getPath().endsWith(".obj"));
            for (Identifier id : found.keySet()) {
                String path = id.getPath();
                int slash = path.lastIndexOf('/');
                int dot = path.lastIndexOf('.');
                if (slash >= 0 && dot > slash) {
                    String base = path.substring(slash + 1, dot);
                    if (!OBJ_BASES.contains(base)) OBJ_BASES.add(base);
                }
            }
        }

        try (Stream<Path> s = Files.exists(USER_MODELS_DIR) ? Files.walk(USER_MODELS_DIR) : Stream.empty()) {
            s.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().toLowerCase().endsWith(".obj"))
                    .forEach(p -> {
                        String rel = USER_MODELS_DIR.relativize(p).toString().replace('\\', '/');
                        String base = rel.substring(0, rel.length() - 4);
                        if (!OBJ_BASES.contains(base)) OBJ_BASES.add(base);
                    });
        } catch (Exception ignored) {}

        Collections.sort(OBJ_BASES, String.CASE_INSENSITIVE_ORDER);
    }

    public static List<String> getModelBases() { return new ArrayList<>(OBJ_BASES); }

    public static Path getUserModelsDir() { return USER_MODELS_DIR; }

    public static Path resolveUserObj(String base) {
        if (base == null || base.isEmpty()) return null;
        Path p = USER_MODELS_DIR.resolve(base.replace('\\','/')).normalize().toAbsolutePath();
        if (!p.getFileName().toString().toLowerCase().endsWith(".obj")) p = p.resolveSibling(p.getFileName().toString() + ".obj");
        return Files.exists(p) ? p : null;
    }

    public static Path resolveUserPng(String base) {
        if (base == null || base.isEmpty()) return null;
        Path p = USER_MODELS_DIR.resolve(base.replace('\\','/')).normalize().toAbsolutePath();
        if (!p.getFileName().toString().toLowerCase().endsWith(".png")) p = p.resolveSibling(p.getFileName().toString() + ".png");
        return Files.exists(p) ? p : null;
    }

    public static Path resolveUserEmissivePng(String base) {
        if (base == null || base.isEmpty()) return null;
        Path p = USER_MODELS_DIR.resolve(base.replace('\\','/')).normalize().toAbsolutePath();
        String name = p.getFileName().toString();
        if (name.toLowerCase().endsWith(".png")) name = name.substring(0, name.length() - 4);
        p = p.resolveSibling(name + "_e.png");
        return Files.exists(p) ? p : null;
    }
}
