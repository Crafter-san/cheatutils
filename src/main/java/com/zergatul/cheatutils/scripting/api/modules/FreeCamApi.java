package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FreeCamConfig;
import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;

public class FreeCamApi {

    private final Minecraft mc = Minecraft.getInstance();

    public boolean isEnabled() {
        return FreeCamController.instance.isActive();
    }

    @ApiVisibility(ApiType.OVERLAY)
    public String getCoordinates() {
        FreeCamController fc = FreeCamController.instance;
        if (fc.isActive()) {
            return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", fc.getX(), fc.getY(), fc.getZ());
        } else {
            return "";
        }
    }

    @ApiVisibility(ApiType.OVERLAY)
    public String getTargetBlockCoordinates() {
        if (mc.level == null || !FreeCamController.instance.isActive()) {
            return "";
        }

        HitResult hitResult = FreeCamController.instance.getHitResult();
        if (hitResult == null) {
            return "";
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            return blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ();
        } else {
            return "";
        }
    }

    @ApiVisibility(ApiType.OVERLAY)
    public String getTargetBlockName() {
        if (mc.level == null || !FreeCamController.instance.isActive()) {
            return "";
        }

        HitResult hitResult = FreeCamController.instance.getHitResult();
        if (hitResult == null) {
            return "";
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState blockState = mc.level.getBlockState(blockPos);
            return ModApiWrapper.BLOCKS.getKey(blockState.getBlock()).toString();
        } else {
            return "";
        }
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        FreeCamController.instance.toggle();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleRenderHands() {
        var config = getConfig();
        config.renderHands = !config.renderHands;
        ConfigStore.instance.requestWrite();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setRenderHands(boolean value) {
        var config = getConfig();
        config.renderHands = value;
        ConfigStore.instance.requestWrite();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void startPath() {
        FreeCamController.instance.startPath();
    }

    private FreeCamConfig getConfig() {
        return ConfigStore.instance.getConfig().freeCamConfig;
    }
}