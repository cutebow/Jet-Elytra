package me.cutebow.jetely.model;

import me.cutebow.jetely.JetElyClient;
import me.cutebow.jetely.util.AssetScanner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.nio.file.Path;

public final class ObjLoader {
    private static final Map<String, ObjMesh> CACHE = new HashMap<>();
    private static final Map<String, Identifier> TEX_CACHE = new HashMap<>();
    private static final Map<String, Identifier> GLOW_CACHE = new HashMap<>();
    private static final String NS = JetElyClient.ASSET_NS;
    private static final String USER_NS = "jetely_user";

    private ObjLoader() {}

    public static ObjMesh load(String base) {
        return CACHE.computeIfAbsent(base, ObjLoader::read);
    }

    private static ObjMesh read(String base) {
        Identifier objId = Identifier.of(NS, "models/custom/" + base + ".obj");
        Optional<Resource> res = MinecraftClient.getInstance().getResourceManager().getResource(objId);
        if (res.isPresent()) {
            try (var in = res.get().getInputStream();
                 var br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return parseObj(br);
            } catch (Exception e) {
                return null;
            }
        }
        Path userObj = AssetScanner.resolveUserObj(base);
        if (userObj != null) {
            try (var br = Files.newBufferedReader(userObj, StandardCharsets.UTF_8)) {
                return parseObj(br);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static ObjMesh parseObj(BufferedReader br) throws Exception {
        List<float[]> pos = new ArrayList<>();
        List<float[]> uv = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<float[]> outPos = new ArrayList<>();
        List<float[]> outUv = new ArrayList<>();
        Map<Long, Integer> remap = new HashMap<>();

        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("v ")) {
                String[] s = split(line, 4, ' ');
                pos.add(new float[]{Float.parseFloat(s[1]), Float.parseFloat(s[2]), Float.parseFloat(s[3])});
            } else if (line.startsWith("vt ")) {
                String[] s = split(line, 3, ' ');
                float u0 = Float.parseFloat(s[1]);
                float v0 = 1.0f - Float.parseFloat(s[2]);
                uv.add(new float[]{u0, v0});
            } else if (line.startsWith("f ")) {
                String[] t = split(line, -1, ' ');
                if (t.length < 4) continue;

                int[] fv = new int[t.length - 1];
                int[] ft = new int[t.length - 1];
                for (int i = 1; i < t.length; i++) {
                    int[] pair = parseVT(t[i], pos.size(), uv.size());
                    fv[i - 1] = pair[0];
                    ft[i - 1] = pair[1];
                }

                for (int i = 1; i + 1 < fv.length; i++) {
                    addVertex(fv[0], ft[0], pos, uv, outPos, outUv, remap, indices);
                    addVertex(fv[i], ft[i], pos, uv, outPos, outUv, remap, indices);
                    addVertex(fv[i + 1], ft[i + 1], pos, uv, outPos, outUv, remap, indices);
                }
            }
        }

        float[] P = flatten3(outPos);
        float[] T = flatten2(outUv);
        int[] I = indices.stream().mapToInt(i -> i).toArray();
        return new ObjMesh(P, T, I);
    }

    private static void addVertex(int vi, int ti,
                                  List<float[]> pos, List<float[]> uv,
                                  List<float[]> outPos, List<float[]> outUv,
                                  Map<Long, Integer> remap, List<Integer> indices) {
        long key = (((long) vi) << 32) | (ti & 0xffffffffL);
        Integer mapped = remap.get(key);
        if (mapped == null) {
            float[] p = pos.get(vi);
            float[] t = (ti >= 0 && ti < uv.size()) ? uv.get(ti) : new float[]{0f, 0f};
            mapped = outPos.size();
            outPos.add(new float[]{p[0], p[1], p[2]});
            outUv.add(new float[]{t[0], t[1]});
            remap.put(key, mapped);
        }
        indices.add(mapped);
    }

    private static int[] parseVT(String tok, int posCount, int uvCount) {
        int v = 0, t = 0;
        int slash = tok.indexOf('/');
        if (slash < 0) {
            v = parseIndex(tok, posCount);
            t = 0;
        } else {
            String sv = tok.substring(0, slash);
            int slash2 = tok.indexOf('/', slash + 1);
            String st = slash2 < 0 ? tok.substring(slash + 1) : tok.substring(slash + 1, slash2);
            v = parseIndex(sv, posCount);
            t = st.isEmpty() ? -1 : parseIndex(st, uvCount);
        }
        return new int[]{v, t};
    }

    private static int parseIndex(String s, int size) {
        int i = Integer.parseInt(s);
        if (i < 0) return size + i;
        return i - 1;
    }

    private static String[] split(String s, int expected, char sep) {
        if (expected > 0) {
            String[] out = new String[expected];
            int n = 0, i = 0, len = s.length();
            while (i < len && n < expected) {
                while (i < len && s.charAt(i) == sep) i++;
                int j = i;
                while (j < len && s.charAt(j) != sep) j++;
                out[n++] = s.substring(i, j);
                i = j + 1;
            }
            return out;
        }
        return s.split("\\s+");
    }

    private static float[] flatten3(List<float[]> a) {
        float[] out = new float[a.size() * 3];
        int k = 0;
        for (float[] v : a) {
            out[k++] = v[0];
            out[k++] = v[1];
            out[k++] = v[2];
        }
        return out;
    }

    private static float[] flatten2(List<float[]> a) {
        float[] out = new float[a.size() * 2];
        int k = 0;
        for (float[] v : a) {
            out[k++] = v[0];
            out[k++] = v[1];
        }
        return out;
    }

    public static Identifier getTexture(String base) {
        Path user = AssetScanner.resolveUserPng(base);
        if (user != null) {
            return TEX_CACHE.computeIfAbsent(base, b -> registerDynamic(user, USER_NS, "user/" + b.replace('\\','/')));
        }
        return Identifier.of(NS, "textures/models/" + base + ".png");
    }

    public static Identifier getEmissiveFor(Identifier baseTexture) {
        if (isDynamic(baseTexture)) {
            String path = baseTexture.getPath();
            String core = path.endsWith(".png") ? path.substring(0, path.length() - 4) : path;
            String base = core.startsWith("user/") ? core.substring("user/".length()) : core;
            Path glow = AssetScanner.resolveUserEmissivePng(base);
            if (glow != null) {
                return GLOW_CACHE.computeIfAbsent(base, b -> registerDynamic(glow, USER_NS, "user/" + b + "_e"));
            }
            return null;
        } else {
            String glowPath = baseTexture.getPath().replace(".png", "_e.png");
            Identifier id = Identifier.of(baseTexture.getNamespace(), glowPath);
            if (MinecraftClient.getInstance().getResourceManager().getResource(id).isPresent()) return id;
            return null;
        }
    }

    public static boolean isDynamic(Identifier id) {
        return USER_NS.equals(id.getNamespace());
    }

    private static Identifier registerDynamic(Path png, String ns, String pathNoExt) {
        try {
            NativeImage img = NativeImage.read(Files.newInputStream(png));
            NativeImageBackedTexture tex = new NativeImageBackedTexture(img);
            Identifier id = Identifier.of(ns, pathNoExt + ".png");
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
            return id;
        } catch (Exception e) {
            return Identifier.of(ns, pathNoExt + ".png");
        }
    }
}
