package net.qxeii.hardcore_torches;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.state.property.Properties.LEVEL_15;

public class ClientMod implements ClientModInitializer, RenderAttachmentBlockEntity {
	@Override
	public void onInitializeClient() {
		// This makes it so the torches don't render with black instead of transparency

		BlockRenderLayerMap.INSTANCE.putBlock(Mod.LIT_TORCH, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.UNLIT_TORCH, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.SMOLDERING_TORCH, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.BURNT_TORCH, RenderLayer.getCutout());

		BlockRenderLayerMap.INSTANCE.putBlock(Mod.LIT_WALL_TORCH, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.UNLIT_WALL_TORCH, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.SMOLDERING_WALL_TORCH, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.BURNT_WALL_TORCH, RenderLayer.getCutout());

		BlockRenderLayerMap.INSTANCE.putBlock(Mod.LIT_LANTERN, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.UNLIT_LANTERN, RenderLayer.getCutout());

		BlockRenderLayerMap.INSTANCE.putBlock(Mod.CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.BLACK_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.BLUE_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.BROWN_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.CYAN_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.GRAY_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.GREEN_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.LIGHT_GRAY_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.LIGHT_BLUE_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.LIME_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.MAGENTA_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.ORANGE_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.PINK_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.PURPLE_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.RED_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.WHITE_CANDLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.YELLOW_CANDLE, RenderLayer.getCutout());

		BlockRenderLayerMap.INSTANCE.putBlock(Mod.GLOWSTONE, RenderLayer.getCutout());
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			float lerp = state.get(LEVEL_15) / 15.0F;
			return ColorHelper.Argb.lerp(lerp, 0x44403B, 0xFFFFFF);
		}, Mod.GLOWSTONE);
		BlockRenderLayerMap.INSTANCE.putBlock(Mod.SHROOMLIGHT, RenderLayer.getCutout());
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			float lerp = state.get(LEVEL_15) / 15.0F;
			return ColorHelper.Argb.lerp(lerp, 0x44403B, 0xFFFFFF);
		}, Mod.SHROOMLIGHT);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
			NbtCompound nbt = stack.getNbt();
			int fuel = Mod.config.defaultShroomlightFuel;
			if (nbt != null) {
				fuel = nbt.getInt("Fuel");
			} else {
				nbt = new NbtCompound();
			}
			float lerp = (float) fuel / Mod.config.defaultShroomlightFuel;
			return ColorHelper.Argb.lerp(lerp, 0x44403B, 0xFFFFFF);
		}, Mod.SHROOMLIGHT);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
			NbtCompound nbt = stack.getNbt();
			int fuel = Mod.config.defaultGlowstoneFuel;
			if (nbt != null) {
				fuel = nbt.getInt("Fuel");
			} else {
				nbt = new NbtCompound();
			}
			float lerp = (float) fuel / Mod.config.defaultGlowstoneFuel;
			return ColorHelper.Argb.lerp(lerp, 0x44403B, 0xFFFFFF);
		}, Mod.GLOWSTONE);
	}

	@Override
	public @Nullable Object getRenderAttachmentData() {
		return null;
	}
}
