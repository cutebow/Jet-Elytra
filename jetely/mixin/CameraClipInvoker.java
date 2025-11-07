package me.cutebow.jetely.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraClipInvoker {
    @Invoker("clipToSpace")
    float jetely$clipToSpace(float distance);
}
