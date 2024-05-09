package net.qxeii.hardcore_torches;

import static net.qxeii.hardcore_torches.block.CandleBlock.STATE_TO_LUMINANCE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.qxeii.hardcore_torches.block.CandleBlock;
import net.qxeii.hardcore_torches.block.FloorTorchBlock;
import net.qxeii.hardcore_torches.block.GlowstoneBlock;
import net.qxeii.hardcore_torches.block.LanternBlock;
import net.qxeii.hardcore_torches.block.ShroomlightBlock;
import net.qxeii.hardcore_torches.block.WallTorchBlock;
import net.qxeii.hardcore_torches.blockentity.CandleBlockEntity;
import net.qxeii.hardcore_torches.blockentity.GlowstoneBlockEntity;
import net.qxeii.hardcore_torches.blockentity.LanternBlockEntity;
import net.qxeii.hardcore_torches.blockentity.ShroomlightBlockEntity;
import net.qxeii.hardcore_torches.blockentity.TorchBlockEntity;
import net.qxeii.hardcore_torches.config.ModConfig;
import net.qxeii.hardcore_torches.item.CandleItem;
import net.qxeii.hardcore_torches.item.FuelCanItem;
import net.qxeii.hardcore_torches.item.GlowstoneItem;
import net.qxeii.hardcore_torches.item.LanternItem;
import net.qxeii.hardcore_torches.item.ShroomlightItem;
import net.qxeii.hardcore_torches.item.TorchItem;
import net.qxeii.hardcore_torches.loot.HCTLootNumberProviderTypes;
import net.qxeii.hardcore_torches.loot.LanternLootFunction;
import net.qxeii.hardcore_torches.loot.TorchLootFunction;
import net.qxeii.hardcore_torches.recipe.CandleRecipe;
import net.qxeii.hardcore_torches.recipe.FuelCanRecipe;
import net.qxeii.hardcore_torches.recipe.UnlitTorchRecipe;
import net.qxeii.hardcore_torches.util.ETorchState;
import net.qxeii.hardcore_torches.util.TorchGroup;

public class Mod implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("hardcore_torches");

	public static ModConfig config;

	// Tags

	public static final TagKey<Item> ALL_TORCH_ITEMS = TagKey.of(RegistryKeys.ITEM,
			new Identifier("hardcore_torches", "torches"));
	public static final TagKey<Item> UNBREAKING_LIGHTER_ITEMS = TagKey.of(RegistryKeys.ITEM,
			new Identifier("hardcore_torches", "unbreaking_lighter_items"));
	public static final TagKey<Item> MULTI_USE_LIGHTER_ITEMS = TagKey.of(RegistryKeys.ITEM,
			new Identifier("hardcore_torches", "multi_use_lighter_items"));
	public static final TagKey<Item> SINGLE_USE_LIGHTER_ITEMS = TagKey.of(RegistryKeys.ITEM,
			new Identifier("hardcore_torches", "single_use_lighter_items"));
	public static final TagKey<Item> CANDLES = TagKey.of(RegistryKeys.ITEM, new Identifier("minecraft", "candles"));

	public static final TagKey<Item> LIQUID_FUELS = TagKey.of(RegistryKeys.ITEM,
			new Identifier("hardcore_torches", "liquid_fuels"));
	public static final TagKey<Item> CAMPFIRE_FUELS = TagKey.of(RegistryKeys.ITEM,
			new Identifier("hardcore_torches", "campfire_fuels"));
	public static final TagKey<Item> CAMPFIRE_LOG_FUELS = TagKey.of(RegistryKeys.ITEM,
			new Identifier("minecraft", "logs_that_burn"));
	public static final TagKey<Item> CAMPFIRE_SHOVELS = TagKey.of(RegistryKeys.ITEM,
			new Identifier("minecraft", "shovels"));

	public static final LootFunctionType HARDCORE_TORCH_LOOT_FUNCTION = new LootFunctionType(
			new TorchLootFunction.Serializer());
	public static final LootFunctionType FUEL_LOOT_FUNCTION = new LootFunctionType(
			new LanternLootFunction.Serializer());

	public static final Block LIT_TORCH = new FloorTorchBlock(FabricBlockSettings.create().noCollision()
			.breakInstantly().luminance(state -> 14).sounds(BlockSoundGroup.WOOD), ParticleTypes.FLAME, ETorchState.LIT,
			() -> config.defaultTorchFuel);
	public static final Block UNLIT_TORCH = new FloorTorchBlock(
			FabricBlockSettings.create().noCollision().breakInstantly().sounds(BlockSoundGroup.WOOD), null,
			ETorchState.UNLIT, () -> config.defaultTorchFuel);
	public static final Block SMOLDERING_TORCH = new FloorTorchBlock(FabricBlockSettings.create().noCollision()
			.breakInstantly().luminance(state -> 3).sounds(BlockSoundGroup.WOOD), ParticleTypes.SMOKE,
			ETorchState.SMOLDERING, () -> config.defaultTorchFuel);
	public static final Block BURNT_TORCH = new FloorTorchBlock(
			FabricBlockSettings.create().noCollision().breakInstantly().sounds(BlockSoundGroup.WOOD), null,
			ETorchState.BURNT, () -> config.defaultTorchFuel);

	public static final Block CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block WHITE_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block BLUE_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block RED_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block PURPLE_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block YELLOW_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block BLACK_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block CYAN_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block GRAY_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block BROWN_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block GREEN_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block LIGHT_BLUE_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block LIGHT_GRAY_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block LIME_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block MAGENTA_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block ORANGE_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);
	public static final Block PINK_CANDLE = new CandleBlock(
			FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.CANDLE).luminance(STATE_TO_LUMINANCE),
			() -> config.defaultCandleFuel, false);

	public static final Block GLOWSTONE = new GlowstoneBlock(FabricBlockSettings.create().strength(1)
			.sounds(BlockSoundGroup.GLASS).luminance(GlowstoneBlock.STATE_TO_LUMINANCE),
			() -> config.defaultGlowstoneFuel);

	public static final Block SHROOMLIGHT = new ShroomlightBlock(FabricBlockSettings.create().strength(1)
			.sounds(BlockSoundGroup.SHROOMLIGHT).luminance(ShroomlightBlock.STATE_TO_LUMINANCE),
			() -> config.defaultShroomlightFuel, true);

	public static final Block LIT_WALL_TORCH = new WallTorchBlock(FabricBlockSettings.create().noCollision()
			.breakInstantly().luminance(state -> 14).sounds(BlockSoundGroup.WOOD), ParticleTypes.FLAME, ETorchState.LIT,
			() -> config.defaultTorchFuel);
	public static final Block UNLIT_WALL_TORCH = new WallTorchBlock(
			FabricBlockSettings.create().noCollision().breakInstantly().sounds(BlockSoundGroup.WOOD), null,
			ETorchState.UNLIT, () -> config.defaultTorchFuel);
	public static final Block SMOLDERING_WALL_TORCH = new WallTorchBlock(FabricBlockSettings.create().noCollision()
			.breakInstantly().luminance(state -> 3).sounds(BlockSoundGroup.WOOD), ParticleTypes.FLAME,
			ETorchState.SMOLDERING, () -> config.defaultTorchFuel);
	public static final Block BURNT_WALL_TORCH = new WallTorchBlock(
			FabricBlockSettings.create().noCollision().breakInstantly().sounds(BlockSoundGroup.WOOD), null,
			ETorchState.BURNT, () -> config.defaultTorchFuel);

	public static final Block LIT_LANTERN = new LanternBlock(FabricBlockSettings.create().noCollision().breakInstantly()
			.luminance(state -> 15).sounds(BlockSoundGroup.LANTERN), true, () -> config.defaultLanternFuel);
	public static final Block UNLIT_LANTERN = new LanternBlock(
			FabricBlockSettings.create().noCollision().breakInstantly().sounds(BlockSoundGroup.LANTERN), false,
			() -> config.defaultLanternFuel);

	public static final Item FUEL_CAN = new FuelCanItem(new FabricItemSettings().maxCount(1));

	public static TorchGroup basicTorches = new TorchGroup("basic");

	// Block Entities

	public static BlockEntityType<TorchBlockEntity> TORCH_BLOCK_ENTITY;
	public static BlockEntityType<LanternBlockEntity> LANTERN_BLOCK_ENTITY;
	public static BlockEntityType<CandleBlockEntity> CANDLE_BLOCK_ENTITY;
	public static BlockEntityType<GlowstoneBlockEntity> GLOWSTONE_BLOCK_ENTITY;
	public static BlockEntityType<ShroomlightBlockEntity> SHROOMLIGHT_BLOCK_ENTITY;

	// Sounds

	public static final SoundEvent CAMPFIRE_LOG_PLACE_SOUND = SoundEvent
			.of(new Identifier("hardcore_torches:log_place"));

	// Recipe Types

	public static final RecipeType<FuelCanRecipe> FUEL_CAN_RECIPE = RecipeType.register("hardcore_torches:fuel_can");
	public static final RecipeType<UnlitTorchRecipe> TORCH_RECIPE = RecipeType.register("hardcore_torches:torch");
	public static final RecipeType<CandleRecipe> CANDLE_RECIPE = RecipeType.register("hardcore_torches:candle");

	public static RecipeSerializer<FuelCanRecipe> FUEL_RECIPE_SERIALIZER;
	public static RecipeSerializer<UnlitTorchRecipe> UNLIT_TORCH_RECIPE_SERIALIZER;
	public static RecipeSerializer<CandleRecipe> CANDLE_RECIPE_SERIALIZER;

	@Override
	public void onInitialize() {
		HCTLootNumberProviderTypes.loadThisClass();

		basicTorches.add(LIT_TORCH);
		basicTorches.add(UNLIT_TORCH);
		basicTorches.add(SMOLDERING_TORCH);
		basicTorches.add(BURNT_TORCH);
		basicTorches.add(LIT_WALL_TORCH);
		basicTorches.add(UNLIT_WALL_TORCH);
		basicTorches.add(SMOLDERING_WALL_TORCH);
		basicTorches.add(BURNT_WALL_TORCH);

		Block[] teTorchBlocks = new Block[] {
				LIT_TORCH,
				UNLIT_TORCH,
				SMOLDERING_TORCH,
				BURNT_TORCH,
				LIT_WALL_TORCH,
				UNLIT_WALL_TORCH,
				SMOLDERING_WALL_TORCH,
				BURNT_WALL_TORCH
		};

		Block[] teLanternBlocks = new Block[] {
				LIT_LANTERN,
				UNLIT_LANTERN
		};

		Block[] teCandleBlocks = new Block[] {
				CANDLE,
				WHITE_CANDLE,
				BLUE_CANDLE,
				BLACK_CANDLE,
				BROWN_CANDLE,
				CYAN_CANDLE,
				GRAY_CANDLE,
				GREEN_CANDLE,
				LIGHT_BLUE_CANDLE,
				LIGHT_GRAY_CANDLE,
				LIME_CANDLE,
				MAGENTA_CANDLE,
				ORANGE_CANDLE,
				PINK_CANDLE,
				PURPLE_CANDLE,
				RED_CANDLE,
				YELLOW_CANDLE
		};

		Block[] teGlowstoneBlocks = new Block[] {
				GLOWSTONE
		};

		Block[] teShroomlightBlocks = new Block[] {
				SHROOMLIGHT
		};

		AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		Registry.register(Registries.LOOT_FUNCTION_TYPE, "hardcore_torches:torch", HARDCORE_TORCH_LOOT_FUNCTION);
		Registry.register(Registries.LOOT_FUNCTION_TYPE, "hardcore_torches:set_damage", FUEL_LOOT_FUNCTION);

		TORCH_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "hardcore_torches:torch_block_entity",
				FabricBlockEntityTypeBuilder.create(TorchBlockEntity::new, teTorchBlocks).build(null));
		CANDLE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "hardcore_torches:candle_block_entity",
				FabricBlockEntityTypeBuilder.create(CandleBlockEntity::new, teCandleBlocks).build(null));
		LANTERN_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "hardcore_torches:lantern_entity",
				FabricBlockEntityTypeBuilder.create(LanternBlockEntity::new, teLanternBlocks).build(null));
		GLOWSTONE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "hardcore_torches:glowstone_entity",
				FabricBlockEntityTypeBuilder.create(GlowstoneBlockEntity::new, teGlowstoneBlocks).build(null));
		SHROOMLIGHT_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				"hardcore_torches:shroomlight_entity",
				FabricBlockEntityTypeBuilder.create(ShroomlightBlockEntity::new, teShroomlightBlocks).build(null));

		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "lit_torch"), LIT_TORCH);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "unlit_torch"), UNLIT_TORCH);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "smoldering_torch"), SMOLDERING_TORCH);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "burnt_torch"), BURNT_TORCH);

		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "lit_wall_torch"), LIT_WALL_TORCH);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "unlit_wall_torch"), UNLIT_WALL_TORCH);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "smoldering_wall_torch"),
				SMOLDERING_WALL_TORCH);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "burnt_wall_torch"), BURNT_WALL_TORCH);

		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "candle"), CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "black_candle"), BLACK_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "blue_candle"), BLUE_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "brown_candle"), BROWN_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "cyan_candle"), CYAN_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "gray_candle"), GRAY_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "green_candle"), GREEN_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "light_blue_candle"), LIGHT_BLUE_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "light_gray_candle"), LIGHT_GRAY_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "lime_candle"), LIME_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "magenta_candle"), MAGENTA_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "orange_candle"), ORANGE_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "pink_candle"), PINK_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "purple_candle"), PURPLE_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "red_candle"), RED_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "white_candle"), WHITE_CANDLE);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "yellow_candle"), YELLOW_CANDLE);

		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "lit_lantern"), LIT_LANTERN);
		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "unlit_lantern"), UNLIT_LANTERN);

		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "glowstone"), GLOWSTONE);

		Registry.register(Registries.BLOCK, new Identifier("hardcore_torches", "shroomlight"), SHROOMLIGHT);

		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "lit_torch"), new TorchItem(LIT_TORCH,
				LIT_WALL_TORCH, new FabricItemSettings(), ETorchState.LIT, config.defaultTorchFuel, basicTorches));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "unlit_torch"), new TorchItem(UNLIT_TORCH,
				UNLIT_WALL_TORCH, new FabricItemSettings(), ETorchState.UNLIT, config.defaultTorchFuel, basicTorches));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "smoldering_torch"),
				new TorchItem(SMOLDERING_TORCH, SMOLDERING_WALL_TORCH, new FabricItemSettings(), ETorchState.SMOLDERING,
						config.defaultTorchFuel, basicTorches));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "burnt_torch"), new TorchItem(BURNT_TORCH,
				BURNT_WALL_TORCH, new FabricItemSettings(), ETorchState.BURNT, config.defaultTorchFuel, basicTorches));

		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "lit_lantern"),
				new LanternItem(LIT_LANTERN, new FabricItemSettings().maxCount(1), config.defaultLanternFuel, true));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "unlit_lantern"),
				new LanternItem(UNLIT_LANTERN, new FabricItemSettings().maxCount(1), config.defaultLanternFuel, false));

		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "candle"),
				new CandleItem(CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "black_candle"),
				new CandleItem(BLACK_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "blue_candle"),
				new CandleItem(BLUE_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "brown_candle"),
				new CandleItem(BROWN_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "cyan_candle"),
				new CandleItem(CYAN_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "gray_candle"),
				new CandleItem(GRAY_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "green_candle"),
				new CandleItem(GREEN_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "light_blue_candle"),
				new CandleItem(LIGHT_BLUE_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "light_gray_candle"),
				new CandleItem(LIGHT_GRAY_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "lime_candle"),
				new CandleItem(LIME_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "magenta_candle"),
				new CandleItem(MAGENTA_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "orange_candle"),
				new CandleItem(ORANGE_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "pink_candle"),
				new CandleItem(PINK_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "purple_candle"),
				new CandleItem(PURPLE_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "red_candle"),
				new CandleItem(RED_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "white_candle"),
				new CandleItem(WHITE_CANDLE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "yellow_candle"),
				new CandleItem(YELLOW_CANDLE, new FabricItemSettings()));

		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "glowstone"),
				new GlowstoneItem(GLOWSTONE, new FabricItemSettings(), Mod.config.defaultGlowstoneFuel, true));

		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "shroomlight"),
				new ShroomlightItem(SHROOMLIGHT, new FabricItemSettings(), Mod.config.defaultShroomlightFuel, true));

		Registry.register(Registries.ITEM, new Identifier("hardcore_torches", "fuel_can"), FUEL_CAN);

		Registry.register(Registries.SOUND_EVENT, CAMPFIRE_LOG_PLACE_SOUND.getId(), CAMPFIRE_LOG_PLACE_SOUND);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(FUEL_CAN));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
				.register(entries -> FuelCanItem.setFuel(new ItemStack(FUEL_CAN), Mod.config.maxCanFuel));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(LIT_TORCH));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(UNLIT_TORCH));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(SMOLDERING_TORCH));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(BURNT_TORCH));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(LIT_LANTERN));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(UNLIT_LANTERN));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(BURNT_TORCH));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(LIT_LANTERN));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(UNLIT_LANTERN));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(BLACK_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(BLUE_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(BROWN_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(CYAN_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(GRAY_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(GREEN_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(LIGHT_BLUE_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(LIGHT_GRAY_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(LIME_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(MAGENTA_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(ORANGE_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(PINK_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(PURPLE_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(RED_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(WHITE_CANDLE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(YELLOW_CANDLE));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(GLOWSTONE));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(SHROOMLIGHT));

		// Recipe Types
		FUEL_RECIPE_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
				new Identifier("hardcore_torches", "fuel_can"), new FuelCanRecipe.Serializer());
		UNLIT_TORCH_RECIPE_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
				new Identifier("hardcore_torches", "unlit_torch"), new UnlitTorchRecipe.Serializer());
		CANDLE_RECIPE_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
				new Identifier("hardcore_torches", "candle"), new CandleRecipe.Serializer());

	}
}
