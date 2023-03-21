package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.zergatul.cheatutils.interfaces.ItemRendererMixinInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemRenderHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void renderItem(PoseStack matrixStack, LivingEntity entity, ItemStack itemStack, int x, int y) {
        mc.getItemRenderer().renderAndDecorateItem(matrixStack, entity, itemStack, x, y, 123);
        mc.getItemRenderer().renderGuiItemDecorations(matrixStack, mc.font, itemStack, x, y);
    }
    
    public static void renderItem(LivingEntity entity, ItemStack itemStack, double x, double y, double z) {
        if (itemStack.isEmpty()) {
            return;
        }

        ItemRenderer itemRenderer = mc.getItemRenderer();
        TextureManager textureManager = ((ItemRendererMixinInterface) itemRenderer).getTextureManager();

        // copy from
        // Lnet/minecraft/client/renderer/entity/ItemRenderer;tryRenderGuiItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;IIII)V
        BakedModel bakedmodel = itemRenderer.getModel(itemStack, null, entity, 0);
        //itemRenderer.blitOffset = bakedmodel.isGui3d() ? itemRenderer.blitOffset + 50.0F + (float)z : itemRenderer.blitOffset + 50.0F;
        
        // copy from
        // Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V
        textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(0.0F, 0.0F, (float)(50 + (bakedmodel.isGui3d() ? 10 : 0)));
        posestack.translate(x, y, 100.0F);
        posestack.translate(8.0F, 8.0F, 0.0F);
        posestack.scale(1.0F, -1.0F, 1.0F);
        posestack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !bakedmodel.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        itemRenderer.render(itemStack, ItemDisplayContext.GUI, false, posestack1, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
        multibuffersource$buffersource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();

        // copy from
        // Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V
        PoseStack posestack2 = new PoseStack();
        if (itemStack.getCount() != 1) {
            String s = String.valueOf(itemStack.getCount());
            posestack2.translate(0.0F, 0.0F, 200.0F);
            MultiBufferSource.BufferSource multibuffersource$buffersource2 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            mc.font.drawInBatch(s, (float)(x + 19 - 2 - mc.font.width(s)), (float)(y + 6 + 3), 16777215, true, posestack2.last().pose(), multibuffersource$buffersource, Font.DisplayMode.NORMAL, 0, 15728880);
            multibuffersource$buffersource2.endBatch();
        }

        if (itemStack.isBarVisible()) {
            RenderSystem.disableDepthTest();
            //RenderSystem.disableTexture();
            RenderSystem.disableBlend();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            int i = itemStack.getBarWidth();
            int j = itemStack.getBarColor();
            Primitives.fillRect(bufferbuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
            Primitives.fillRect(bufferbuilder, x + 2, y + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
            RenderSystem.enableBlend();
            //RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }

        LocalPlayer localplayer = Minecraft.getInstance().player;
        float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(itemStack.getItem(), Minecraft.getInstance().getFrameTime());
        if (f > 0.0F) {
            RenderSystem.disableDepthTest();
            //RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Tesselator tesselator1 = Tesselator.getInstance();
            BufferBuilder bufferbuilder1 = tesselator1.getBuilder();
            Primitives.fillRect(bufferbuilder1, x, y + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
            //RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }

        //net.minecraftforge.client.ItemDecoratorHandler.of(itemStack).render(mc.font, itemStack, x, y, itemRenderer.blitOffset);
    }
}
