package me.cutebow.jetely;

import me.cutebow.jetely.config.Config;
import me.cutebow.jetely.model.ObjLoader;
import me.cutebow.jetely.model.ObjMesh;
import me.cutebow.jetely.render.ObjRenderer;
import me.cutebow.jetely.util.AssetScanner;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class JetElyClient implements ClientModInitializer {
    public static final String MODID = "jetely";
    public static final String ASSET_NS = "jetely_models";

    private static final WeakHashMap<UUID, Float> ROLL = new WeakHashMap<>();
    private static final WeakHashMap<UUID, Float> ROLL_VEL = new WeakHashMap<>();
    private static final Set<UUID> MADE_INVISIBLE = new HashSet<>();

    private static KeyBinding BOMB_KEY;
    private static long lastBombMs = 0L;

    @Override
    public void onInitializeClient() {
        Config.load();
        AssetScanner.init();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(AssetScanner.reloadListener());

        BOMB_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + MODID + ".drop_bomb",
                GLFW.GLFW_KEY_B,
                "key.categories.gameplay"
        ));

        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null) return;
            if (!Config.get().general.enabled) return;
            float td = mc.getRenderTickCounter().getTickDelta(false);
            MatrixStack ms = ctx.matrixStack();
            VertexConsumerProvider vcp = mc.getBufferBuilders().getEntityVertexConsumers();

            var cfg = Config.get();
            var names = AssetScanner.getModelBases();

            for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
                if (!shouldRenderPlaneFor(p)) continue;

                String base = selectPlaneBaseFor(p, names);
                ObjMesh mesh = ObjLoader.load(base);
                if (mesh == null) continue;

                var mset = cfg.elytra.getPerModel(base);
                float px = (float) MathHelper.lerp(td, p.prevX, p.getX());
                float py = (float) MathHelper.lerp(td, p.prevY, p.getY());
                float pz = (float) MathHelper.lerp(td, p.prevZ, p.getZ());

                float bodyYaw = MathHelper.lerp(td, p.prevBodyYaw, p.bodyYaw);
                float pitch = MathHelper.clamp(p.getPitch(td), -89.0f, 89.0f);

                float yawDiff = MathHelper.wrapDegrees(p.getYaw(td) - bodyYaw);
                float velSide = p.getVelocity().horizontalLength() == 0f ? 0f : (yawDiff / 90.0f);
                velSide = MathHelper.clamp(velSide, -1f, 1f);

                float r = ROLL.getOrDefault(p.getUuid(), 0f);
                float rv = ROLL_VEL.getOrDefault(p.getUuid(), 0f);
                float target = MathHelper.clamp(-velSide * 75f, -70f, 70f);

                rv += (target - r) * 0.12f;
                rv *= 0.82f;
                r += rv;

                if (Math.abs(target) < 0.5f) {
                    r *= 0.92f;
                    rv *= 0.6f;
                }

                if (!p.isFallFlying() || Float.isNaN(r) || Float.isNaN(rv)) {
                    r = 0f;
                    rv = 0f;
                }

                r = MathHelper.clamp(r, -70f, 70f);
                ROLL.put(p.getUuid(), r);
                ROLL_VEL.put(p.getUuid(), rv);

                ms.push();
                double cx = ctx.camera().getPos().x;
                double cy = ctx.camera().getPos().y;
                double cz = ctx.camera().getPos().z;
                ms.translate(px - cx, py - cy + 1.0, pz - cz);
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - bodyYaw));
                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-pitch));
                ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(r));
                ms.translate(mset.offset[0], mset.offset[1], mset.offset[2]);
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(mset.rotation[1]));

                Identifier tex = ObjLoader.getTexture(base);
                ObjRenderer.render(mesh, tex, ms, vcp, 0x00F000F0, mset.scale, 0f, 0f, 0f, 0f, 0f, 0f);
                ms.pop();
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;
            var cfg = Config.get();

            if (!cfg.general.enabled) {
                if (!MADE_INVISIBLE.isEmpty()) {
                    for (var p : client.world.getPlayers()) {
                        if (MADE_INVISIBLE.remove(p.getUuid())) p.setInvisible(false);
                    }
                }
                return;
            }

            if (cfg.bombing.enabled && client.player != null && BOMB_KEY.isPressed()) {
                long now = System.currentTimeMillis();
                if (now - lastBombMs >= Math.max(0, cfg.bombing.bombCooldownMs)) {
                    dropBomb(client);
                    lastBombMs = now;
                }
            }

            if (!cfg.elytra.hidePlayerModelWhileFlying) {
                if (!MADE_INVISIBLE.isEmpty()) {
                    for (var p : client.world.getPlayers()) {
                        if (MADE_INVISIBLE.remove(p.getUuid())) p.setInvisible(false);
                    }
                }
            } else {
                for (AbstractClientPlayerEntity p : client.world.getPlayers()) {
                    if (!shouldRenderPlaneFor(p)) {
                        if (MADE_INVISIBLE.remove(p.getUuid())) p.setInvisible(false);
                        continue;
                    }
                    if (!p.isInvisible()) { p.setInvisible(true); MADE_INVISIBLE.add(p.getUuid()); }
                }
            }
        });
    }

    private static void dropBomb(MinecraftClient mc) {
        var c = Config.get().bombing;
        var p = mc.player;
        if (p == null) return;

        double yaw = Math.toRadians(p.getYaw() + c.yawOffset);
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        double sx = 0.0;
        double sy = -Math.abs(c.dropSpeed);
        double sz = 0.0;

        double bx = p.getX() + (cos * c.offsetZ) + (-sin * c.offsetX);
        double by = p.getY() + c.offsetY;
        double bz = p.getZ() + (sin * c.offsetZ) + (cos * c.offsetX);

        String cmd = "summon tnt_minecart " + f(bx) + " " + f(by) + " " + f(bz) + " {Motion:[" + f(sx) + "," + f(sy) + "," + f(sz) + "]}";
        mc.player.networkHandler.sendChatCommand(cmd);
    }

    private static String f(double v) {
        return String.format(java.util.Locale.ROOT, "%.3f", v);
    }

    private static boolean shouldRenderPlaneFor(AbstractClientPlayerEntity p) {
        if (!p.isFallFlying() || !p.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) return false;
        var me = MinecraftClient.getInstance().player;
        if (me != null && p.getUuid().equals(me.getUuid())) {
            if (MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) return false;
        }
        var cfg = Config.get();
        if (!cfg.general.applyToOthers) {
            if (me == null || !p.getUuid().equals(me.getUuid())) return false;
        }
        return true;
    }

    private static String selectPlaneBaseFor(AbstractClientPlayerEntity p, java.util.List<String> names) {
        var cfg = Config.get();
        if (names.isEmpty()) return cfg.elytra.selectedModel;
        var me = MinecraftClient.getInstance().player;
        boolean isOther = me != null && !p.getUuid().equals(me.getUuid());
        if (isOther && cfg.general.applyToOthers) {
            if (cfg.elytra.randomizeOthers) {
                String cached = cfg.assigned.get(p.getUuid().toString());
                if (cached != null && names.contains(cached)) return cached;
                int i = Math.abs(p.getUuid().hashCode()) % names.size();
                String sel = names.get(i);
                cfg.assigned.put(p.getUuid().toString(), sel);
                Config.save();
                return sel;
            }
            return cfg.elytra.selectedModel;
        }
        return cfg.elytra.selectedModel;
    }
}
