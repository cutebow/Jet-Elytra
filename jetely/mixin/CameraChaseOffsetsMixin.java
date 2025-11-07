package me.cutebow.jetely.mixin;

import me.cutebow.jetely.config.Config;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class CameraChaseOffsetsMixin {
    @Shadow public abstract Vec3d getPos();
    @Shadow public abstract float getYaw();
    @Shadow public abstract void setPos(double x, double y, double z);
    @Shadow public abstract Entity getFocusedEntity();

    @Inject(method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", at = @At("TAIL"))
    private void jetely$applyChaseOffsets(BlockView area, Entity focus, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!Config.get().general.enabled) return;
        if (!thirdPerson) return;
        var cfg = Config.get();
        if (!cfg.general.useChaseCamera) return;
        Entity e = this.getFocusedEntity();
        if (!(e instanceof LivingEntity le) || !le.isFallFlying()) return;
        var set = cfg.elytra.getPerModel(cfg.elytra.selectedModel);
        double back = set.cameraDistance;
        double up   = set.cameraHeight;
        if (back == 0.0 && up == 0.0) return;
        float yawRad = (float) Math.toRadians(this.getYaw());
        double dx = -MathHelper.sin(yawRad) * back;
        double dz =  MathHelper.cos(yawRad) * back;
        Vec3d p = this.getPos();
        this.setPos(p.x + dx, p.y + up, p.z + dz);
    }
}
