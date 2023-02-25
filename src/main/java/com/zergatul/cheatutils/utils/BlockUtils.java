package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static PlaceBlockPlan getPlacingPlan(BlockPos pos) {
        if (mc.level == null) {
            return null;
        }

        BlockState state = mc.level.getBlockState(pos);
        if (!state.getMaterial().isReplaceable()) {
            return null;
        }

        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pos.relative(direction);
            BlockState neighbourState = mc.level.getBlockState(neighbourPos);
            if (!neighbourState.getMaterial().isReplaceable()) {
                return new PlaceBlockPlan(pos.immutable(), direction.getOpposite(), neighbourPos);
            }
        }

        return null;
    }

    public static void applyPlacingPlan(PlaceBlockPlan plan, boolean useShift) {
        placeBlock(plan.destination, plan.direction, plan.neighbour, useShift);
    }

    public static boolean placeBlock(BlockPos pos) {
        if (mc.level == null) {
            return false;
        }

        BlockState blockState = mc.level.getBlockState(pos);
        if (!blockState.getMaterial().isReplaceable()) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            BlockPos other = pos.relative(direction);
            BlockState state = mc.level.getBlockState(other);
            if (!state.getShape(mc.level, other).isEmpty()) {
                placeBlock(pos, direction.getOpposite(), other, true);
                return true;
            }
        }

        return false;
    }

    private static void placeBlock(BlockPos destination, Direction direction, BlockPos neighbour, boolean useShift) {
        if (mc.player == null) {
            return;
        }

        Vec3 location = new Vec3(
                destination.getX() + 0.5f + direction.getOpposite().getStepX() * 0.5,
                destination.getY() + 0.5f + direction.getOpposite().getStepY() * 0.5,
                destination.getZ() + 0.5f + direction.getOpposite().getStepZ() * 0.5);
        BlockHitResult hit = new BlockHitResult(location, direction, neighbour, false);

        boolean emulateShift = useShift && !mc.player.isShiftKeyDown();

        if (emulateShift) {
            NetworkPacketsController.instance.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        }

        InteractionHand hand = InteractionHand.MAIN_HAND;
        InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
        if (result.consumesAction()) {
            if (result.shouldSwing()) {
                mc.player.swing(hand);
            }
        }

        if (emulateShift) {
            NetworkPacketsController.instance.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        }
    }

    public record PlaceBlockPlan(BlockPos destination, Direction direction, BlockPos neighbour) {}
}