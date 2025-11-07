package me.cutebow.jetely.render;

import me.cutebow.jetely.config.Config;
import me.cutebow.jetely.model.ObjLoader;
import me.cutebow.jetely.model.ObjMesh;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public final class ObjRenderer {
    private ObjRenderer() {}

    public static void render(ObjMesh mesh, Identifier texture, MatrixStack matrices, VertexConsumerProvider provider, int light, float scale, float ox, float oy, float oz, float yaw, float pitch, float roll) {
        if (!Config.get().general.enabled) return;
        VertexConsumer vc = provider.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
        matrices.push();
        matrices.translate(ox, oy, oz);
        matrices.scale(scale, scale, scale);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
        MatrixStack.Entry entry = matrices.peek();

        for (int i = 0; i + 2 < mesh.indices.length; i += 3) {
            int ia = mesh.indices[i];
            int ib = mesh.indices[i + 1];
            int ic = mesh.indices[i + 2];

            float ax = mesh.positions[ia * 3];
            float ay = mesh.positions[ia * 3 + 1];
            float az = mesh.positions[ia * 3 + 2];
            float au = mesh.uvs[ia * 2];
            float av = mesh.uvs[ia * 2 + 1];

            float bx = mesh.positions[ib * 3];
            float by = mesh.positions[ib * 3 + 1];
            float bz = mesh.positions[ib * 3 + 2];
            float bu = mesh.uvs[ib * 2];
            float bv = mesh.uvs[ib * 2 + 1];

            float cx = mesh.positions[ic * 3];
            float cy = mesh.positions[ic * 3 + 1];
            float cz = mesh.positions[ic * 3 + 2];
            float cu = mesh.uvs[ic * 2];
            float cv = mesh.uvs[ic * 2 + 1];

            float ux = bx - ax, uy = by - ay, uz = bz - az;
            float vx = cx - ax, vy = cy - ay, vz = cz - az;
            float nx = uy * vz - uz * vy;
            float ny = uz * vx - ux * vz;
            float nz = ux * vy - uy * vx;
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len != 0f) { nx /= len; ny /= len; nz /= len; }

            emit(vc, entry, ax, ay, az, au, av, nx, ny, nz, light);
            emit(vc, entry, bx, by, bz, bu, bv, nx, ny, nz, light);
            emit(vc, entry, cx, cy, cz, cu, cv, nx, ny, nz, light);
            emit(vc, entry, cx, cy, cz, cu, cv, nx, ny, nz, light);
        }

        Identifier glowTex = ObjLoader.getEmissiveFor(texture);
        boolean glowExists = glowTex != null && (ObjLoader.isDynamic(glowTex) || MinecraftClient.getInstance().getResourceManager().getResource(glowTex).isPresent());
        if (glowExists) {
            VertexConsumer glow = provider.getBuffer(RenderLayer.getEyes(glowTex));
            for (int i = 0; i + 2 < mesh.indices.length; i += 3) {
                int ia = mesh.indices[i];
                int ib = mesh.indices[i + 1];
                int ic = mesh.indices[i + 2];

                float ax = mesh.positions[ia * 3];
                float ay = mesh.positions[ia * 3 + 1];
                float az = mesh.positions[ia * 3 + 2];
                float au = mesh.uvs[ia * 2];
                float av = mesh.uvs[ia * 2 + 1];

                float bx = mesh.positions[ib * 3];
                float by = mesh.positions[ib * 3 + 1];
                float bz = mesh.positions[ib * 3 + 2];
                float bu = mesh.uvs[ib * 2];
                float bv = mesh.uvs[ib * 2 + 1];

                float cx = mesh.positions[ic * 3];
                float cy = mesh.positions[ic * 3 + 1];
                float cz = mesh.positions[ic * 3 + 2];
                float cu = mesh.uvs[ic * 2];
                float cv = mesh.uvs[ic * 2 + 1];

                float ux = bx - ax, uy = by - ay, uz = bz - az;
                float vx = cx - ax, vy = cy - ay, vz = cz - az;
                float nx = uy * vz - uz * vy;
                float ny = uz * vx - ux * vz;
                float nz = ux * vy - uy * vx;
                float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (len != 0f) { nx /= len; ny /= len; nz /= len; }

                emit(glow, entry, ax, ay, az, au, av, nx, ny, nz, 0x00F000F0);
                emit(glow, entry, bx, by, bz, bu, bv, nx, ny, nz, 0x00F000F0);
                emit(glow, entry, cx, cy, cz, cu, cv, nx, ny, nz, 0x00F000F0);
                emit(glow, entry, cx, cy, cz, cu, cv, nx, ny, nz, 0x00F000F0);
            }
        }

        matrices.pop();
    }

    private static void emit(VertexConsumer vc, MatrixStack.Entry entry, float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {
        vc.vertex(entry, x, y, z)
                .color(1f, 1f, 1f, 1f)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, nx, ny, nz);
    }
}
