package me.cutebow.jetely.client;

import me.cutebow.jetely.config.Config;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.option.KeyBinding;

import java.util.Locale;

public final class BombingClient {
    private static KeyBinding DROP_BOMB;
    private static long lastDrop = 0L;

    public static void init() {
        DROP_BOMB = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.jetely.drop_bomb", GLFW.GLFW_KEY_B, "key.categories.jetely"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            if (!Config.get().general.enabled) return;
            if (!Config.get().bombing.enabled) return;

            int cd = Math.max(0, Config.get().bombing.bombCooldownMs);

            while (DROP_BOMB.wasPressed()) {
                dropBomb(client);
                lastDrop = System.currentTimeMillis();
            }
            if (DROP_BOMB.isPressed()) {
                long now = System.currentTimeMillis();
                if (now - lastDrop >= cd) {
                    dropBomb(client);
                    lastDrop = now;
                }
            }
        });
    }

    private static void dropBomb(MinecraftClient mc) {
        var c = Config.get().bombing;
        Vec3d p = mc.player.getPos();

        float yaw = mc.player.getYaw() + c.yawOffset;
        float rad = (float) Math.toRadians(yaw);
        double cos = MathHelper.cos(rad);
        double sin = MathHelper.sin(rad);

        double ox = c.offsetX * cos - c.offsetZ * sin;
        double oz = c.offsetX * sin + c.offsetZ * cos;

        double x = p.x + ox;
        double y = p.y + c.offsetY;
        double z = p.z + oz;

        double sx = 0.0;
        double sy = -Math.abs(c.dropSpeed);
        double sz = 0.0;

        String nbt = "{Motion:[" + f(sx) + "," + f(sy) + "," + f(sz) + "],Rotation:[" + f(yaw) + "f,0.0f]}";
        String cmd = "summon tnt_minecart " + f(x) + " " + f(y) + " " + f(z) + " " + nbt;
        mc.player.networkHandler.sendChatCommand(cmd);
    }

    private static String f(double d) {
        return String.format(Locale.ROOT, "%.3f", d);
    }
}
