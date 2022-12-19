package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.StatusOverlayController;
import com.zergatul.cheatutils.scripting.api.HelpText;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public class MainApi {

    private final Minecraft mc = Minecraft.getInstance();

    public void addText(String text) {
        addText("#FFFFFF", text);
    }

    public void addText(String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableComponent component = new TextComponent(text);
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        StatusOverlayController.instance.addText(component);
    }

    public void addText(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableComponent component1 = new TextComponent(text1);
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = new TextComponent(text2);
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        StatusOverlayController.instance.addText(component1.append(" ").append(component2));
    }

    public String getCoordinates() {
        return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", mc.getCameraEntity().getX(), mc.getCameraEntity().getY(), mc.getCameraEntity().getZ());
    }

    public boolean isDebugScreenEnabled() {
        return mc.options.renderDebug;
    }

    public boolean isEspEnabled() {
        return ConfigStore.instance.getConfig().esp;
    }

    @HelpText("Allowed values: \"left\", \"center\", \"right\".")
    public void setOverlayHorizontalPosition(String position) {
        if (position != null) {
            position = position.toLowerCase(Locale.ROOT);
        }
        switch (position) {
            case "left" -> StatusOverlayController.instance.setHorizontalAlign(StatusOverlayController.HorizontalAlign.LEFT);
            case "center" -> StatusOverlayController.instance.setHorizontalAlign(StatusOverlayController.HorizontalAlign.CENTER);
            default -> StatusOverlayController.instance.setHorizontalAlign(StatusOverlayController.HorizontalAlign.RIGHT);
        }
    }

    @HelpText("Allowed values: \"top\", \"middle\", \"bottom\".")
    public void setOverlayVerticalPosition(String position) {
        if (position != null) {
            position = position.toLowerCase(Locale.ROOT);
        }
        switch (position) {
            case "top" -> StatusOverlayController.instance.setVerticalAlign(StatusOverlayController.VerticalAlign.TOP);
            case "middle" -> StatusOverlayController.instance.setVerticalAlign(StatusOverlayController.VerticalAlign.MIDDLE);
            default -> StatusOverlayController.instance.setVerticalAlign(StatusOverlayController.VerticalAlign.BOTTOM);
        }
    }

    /*public String getPlayerEntitySeed() {
        var players =  Minecraft.getInstance().getSingleplayerServer().overworld().players();
        if (players.size() > 0) {
            var rnd = (LegacyRandomSource) players.get(0).getRandom();
            var seed = (AtomicLong) ReflectionUtils.getDeclaredField(rnd, "seed");
            return Long.toHexString(seed.get());
        } else {
            return "";
        }
    }

    public String getEnchantmentSeed() {
        var players =  Minecraft.getInstance().getSingleplayerServer().overworld().players();
        if (players.size() > 0) {
            var seed = players.get(0).getEnchantmentSeed();
            return Integer.toHexString(seed);
        } else {
            return "";
        }
    }

    public int getEnchantmentSeedInt() {
        var players =  Minecraft.getInstance().getSingleplayerServer().overworld().players();
        if (players.size() > 0) {
            return players.get(0).getEnchantmentSeed();
        } else {
            return Integer.MIN_VALUE;
        }
    }*/

    public String getTargetBlockCoordinates() {
        if (mc.level == null) {
            return "";
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return "";
        }

        HitResult result = entity.pick(20.0D, 0.0F, false);
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) result).getBlockPos();
            return blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ();
        } else {
            return "";
        }
    }

    public String getTargetBlockName() {
        if (mc.level == null) {
            return "";
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return "";
        }

        HitResult result = entity.pick(20.0D, 0.0F, false);
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) result).getBlockPos();
            BlockState blockState = mc.level.getBlockState(blockPos);
            return ForgeRegistries.BLOCKS.getKey(blockState.getBlock()).toString();
        } else {
            return "";
        }
    }
}