package com.zergatul.cheatutils.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.helpers.MixinGameRendererHelper;
import com.zergatul.cheatutils.interfaces.GameRendererMixinInterface;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements GameRendererMixinInterface {

    @Shadow
    protected abstract double getFov(Camera camera, float partialTicks, boolean usedConfiguredFov);

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V")
    private void onBeforePick(float vec33, CallbackInfo info) {
        MixinGameRendererHelper.insidePick = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V")
    private void onAfterPick(float vec33, CallbackInfo info) {
        MixinGameRendererHelper.insidePick = false;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"), method = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;F)V", require = 0)
    private void onRenderItemInHand(PoseStack p_109121_, Camera p_109122_, float p_109123_, CallbackInfo info) {
        MixinGameRendererHelper.insideRenderItemInHand = true;
    }

    @Override
    public double getFov(Camera camera, float partialTicks) {
        return getFov(camera, partialTicks, true);
    }
}