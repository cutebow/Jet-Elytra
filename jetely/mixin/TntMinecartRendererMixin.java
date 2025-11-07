package me.cutebow.jetely.mixin;

import me.cutebow.jetely.JetElyClient;
import me.cutebow.jetely.config.Config;
import me.cutebow.jetely.model.ObjLoader;
import me.cutebow.jetely.model.ObjMesh;
import me.cutebow.jetely.render.ObjRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntityRenderer.class)
public class TntMinecartRendererMixin {
    @Inject(method = "render(Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void jetely$renderEntity(AbstractMinecartEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        if (!Config.get().general.enabled) return;
        if (!(entity instanceof TntMinecartEntity)) return;
        var cfg = Config.get();
        if (cfg.tnt.hideVanilla && !cfg.tnt.enabled) { ci.cancel(); return; }
        if (!cfg.tnt.enabled) return;

        String base = cfg.tnt.selectedModel;
        ObjMesh mesh = ObjLoader.load(base);
        if (mesh == null) { if (cfg.tnt.hideVanilla) ci.cancel(); return; }

        var set = cfg.tnt.getPerModel(base);
        matrices.push();
        matrices.translate(0.0, 0.5, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f + set.rotation[1]));
        matrices.translate(set.offset[0], set.offset[1], set.offset[2]);

        Identifier tex = ObjLoader.getTexture(base);
        ObjRenderer.render(mesh, tex, matrices, vcp, light, set.scale, 0f, 0f, 0f, 0f, 0f, 0f);
        matrices.pop();
        ci.cancel();
    }
}
