package me.cutebow.jetely.mixin;

import me.cutebow.jetely.config.Config;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = { "clipToSpace(F)F", "method_19314(F)F" }, at = @At("HEAD"), cancellable = true, require = 0)
    private void jetely$noThirdPersonCollision(float distance, CallbackInfoReturnable<Float> cir) {
        if (!Config.get().general.enabled) return;
        if (Config.get().general.disableThirdPersonClip) {
            cir.setReturnValue(distance);
        }
    }
}
