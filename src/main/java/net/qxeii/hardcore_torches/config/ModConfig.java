package net.qxeii.hardcore_torches.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "hardcore_torches")
public class ModConfig implements ConfigData {

	// Item Handling

	@Comment("Torches will extinguish if broken. Default: false")
	public boolean torchesExtinguishWhenBroken = false;

	@Comment("Torches are fully depleted when broken. Overrides torchesExtinguishWhenBroken. Default: false")
	public boolean torchesDepleteWhenDropped = false;

	@Comment("Burnt torches drop as vanilla stick when broken instead of a burnt torch. Default: false")
	public boolean burntStick = false;

	@Comment("Only matters if torchesRain is true. In rain, torches will extinguish but emit smoke, and consume fuel at 1/3 the rate until fully extinguished or re-lit. Default: true")
	public boolean torchesSmolder = true;

	// Item Handling in Inventory

	@Comment("Do torches lose fuel while the player has then in their inventory. Default: true")
	public boolean tickInInventory = true;

	@Comment("Do torches become unlit when placed in storage. Default: false")
	public boolean unlightInChest = false;

	// Item Interaction

	@Comment("If true, you can right click torches to extinguish them while not holding fuel or a torch to light. Default: true")
	public boolean handUnlightTorch = true;

	@Comment("If true, you can right click lanterns to extinguish them while not holding fuel or a torch to light. Default: true")
	public boolean handUnlightLantern = true;

	@Comment("Allow the player to pick up lanterns with sneak-clicking. Default: true")
	public boolean pickUpLanterns = true;

	// Random Effects

	@Comment("Chance value for a smoldering torch to consume fuel per tick (1/x, default 1/5). Default: 5")
	public int torchesSmolderFuelUseTickChance = 5;

	@Comment("Chance value for a smoldering torch to extinguish and become unlit per tick (1/x, default 1/1000). Default: 1000")
	public int torchesSmolderExtinguishTickChance = 1000;

	// Environmental Effects in World

	@Comment("Torches become unlit in rain. If torchesSmolder is true, they will instead smolder in rain. Default: true")
	public boolean torchesRain = true;

	@Comment("Chance value for a burning torch to extinguish per tick (1/x, default 1/200). Default: 200")
	public int torchesRainAffectTickChance = 200;

	@Comment("Chance value for a burning campfire to extinguish per tick (1/x, default 1/400). Default: 400")
	public int campfiresRainAffectTickChance = 400;

	// Environmental Effects in Inventory

	@Comment("0: When going underwater, torches in your inventory will be unaffected\n1: When going underwater, torches in mainhand or offhand will be extinguished\n2: When going underwater, torches in inventory will be extinguished. Default: 1")
	public int invExtinguishInWater = 1;

	@Comment("0: When in rain, torches in your inventory will be unaffected\n1: When in rain, torches in mainhand or offhand will be extinguished or smolder\n2: When in rain, torches in inventory will be extinguished or smolder. Default: 1")
	public int invExtinguishInRain = 1;

	// Item Fuel Defaults

	@Comment("The amount of ticks the torch lasts (20 ticks per second, 24000 = 20 minutes). Default: 24000")
	public int defaultTorchFuel = 24000;

	@Comment("The amount of ticks the campfire lasts without added fuel (20 ticks per second, 6400 = around 5 minutes). Default: 6400")
	public int defaultCampfireFuel = 6400;

	@Comment("The amount of ticks the lantern can last (20 ticks per second, 24000 = 20 minutes). Default: 24000")
	public int defaultLanternFuel = 24000;

	@Comment("The amount a fuel item adds to the lantern by default. Default: 12000")
	public int defaultLanternFuelItem = 12000;

	@Comment("Default: fuel that a lantern starts with when crafted. Default: 0")
	public int startingLanternFuel = 0;

	@Comment("The fuel that candles start with. Default: 6400")
	public int defaultCandleFuel = 6400;

	@Comment("The fuel that glowstone starts with. Default: 576000")
	public int defaultGlowstoneFuel = 576000;

	@Comment("The fuel that shroomlights start with. Default: 10000")
	public int defaultShroomlightFuel = 10000;

	// Item Fuel Use

	@Comment("Items use up more fuel when held by a player (e.g. 2 means items burn half as long). Default: 2")
	public int itemFuelUseMultiplierWhenHeld = 2;

	@Comment("Items randomly use more or less fuel when held by a player per tick (e.g. 10–100 is minor jitter, 1000 is significant jitter). Default: 0")
	public int itemFuelUseJitterChanceWhenHeld = 0;

	@Comment("Items added to a campfire have their fuel value multiplied (e.g. 4 means one coal adds the fuel value of 4x coal to the fire). Default: 4")
	public int campfireFuelAdditionMultiplier = 4;

	@Comment("The amount of damage to a campfire when it is extinguished (by water, rain, or manually). Default: 800")
	public int campfireExtinguishFuelLoss = 800;

	@Comment("The amount of damage to a torch when it is extinguished (by water, rain, storage, or manually). Default: 800")
	public int torchesExtinguishFuelLoss = 800;

	@Comment("A lantern must have at least this much fuel (min 600) to be ignited from unlit. Once lit it will continue to burn to 0. Default: 400")
	public int minLanternIgnitionFuel = 400;

	@Comment("The amount of fuel wasted in a lantern when it is lit. Used to balance durable lanterns. Default: 200")
	public int lanternLightFuelLoss = 200;

	// Fuel Can

	@Comment("Max fuel an fuel can holds. Default: 576000")
	public int maxCanFuel = 576000;

	@Comment("Can you refuel a torch using an fuel can. Default: false")
	public boolean torchesUseCan = false;

	@Comment("Multiplies the fuel value of all fuel can recipes. 0.5 makes all fuel recipes return half as much fuel. Default: 1")
	public float fuelCanRecipeMultiplier = 1;

	@Comment("Overrides the fuel can fuel recipe if set. Default: -1")
	public int fuelRecipeOverride = -1;

	@Comment("If true, you can craft fuel with an fuel can to fill it. You can also add custom fill recipes with a datapack, open the mod jar to see the JSON format. Default: true")
	public boolean enableCanRefillWithFuel = true;

	@Comment("If true, you can craft coal with an fuel can to fill it. You can also add custom fill recipes with a datapack, open the mod jar to see the JSON format. Default: false")
	public boolean enableCanRefillWithCoal = false;

	@Comment("Right click torch or lantern to see fuel value. Default: true")
	public boolean fuelMessage = true;

	// Item Crafting

	@Comment("How many torches are crafted. Default: 2")
	public int craftAmount = 2;

	@Comment("How many candles are crafted. Default: 4")
	public int candleCraftAmount = 4;

	// World Configuration

	@Comment("The length of daytime in ticks. May be adjusted if other mods alter the length of days. Default: 13000")
	public int worldDaytimeDuration = 13000;

	@Comment("The length of nighttime in ticks. May be adjusted if other mods alter the length of days. Default: 11000")
	public int worldNighttimeDuration = 11000;

	// Vanilla to Mod Item Conversion

	@Comment("Picking up vanilla torches converts them to lit torches. Default: false")
	public boolean convertVanillaTorches = false;

	@Comment("Picking up vanilla lanterns converts them to lit torches. Default: false")
	public boolean convertVanillaLanterns = false;

	@Comment("Picked up vanilla torches will be converted as already lit. Default: false")
	public boolean torchesBurnWhenConverted = false;

	@Comment("Picked up vanilla lanterns will be converted as already lit. Default: false")
	public boolean lanternsBurnWhenConverted = false;

}