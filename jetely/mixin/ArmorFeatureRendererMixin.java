package me.cutebow.jetely.mixin;

import me.cutebow.jetely.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void jetely$hideArmor(MatrixStack matrices, VertexConsumerProvider providers, int light,
                                  LivingEntity entity, float limbAngle, float limbDistance, float tickDelta,
                                  float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (!Config.get().general.enabled) return;
        if (!(entity instanceof AbstractClientPlayerEntity p)) return;
        var cfg = Config.get();
        if (!cfg.elytra.hidePlayerModelWhileFlying) return;
        if (!p.isFallFlying() || !p.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) return;
        if (!cfg.general.applyToOthers) {
            var me = MinecraftClient.getInstance().player;
            if (me == null || !p.getUuid().equals(me.getUuid())) return;
        }
        ci.cancel();
    }
}
