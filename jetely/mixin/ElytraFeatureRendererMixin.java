package me.cutebow.jetely.mixin;

import me.cutebow.jetely.config.Config;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraFeatureRenderer.class)
public abstract class ElytraFeatureRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void jetely$hideElytra(MatrixStack matrices, VertexConsumerProvider providers, int light,
                                   LivingEntity entity, float limbAngle, float limbDistance,
                                   float tickDelta, float customAngle, float headYaw, float headPitch,
                                   CallbackInfo ci) {
        if (!Config.get().general.enabled) return;
        if (entity instanceof AbstractClientPlayerEntity p && p.isFallFlying()) ci.cancel();
    }
}
