package net.qxeii.hardcore_torches.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "hardcore_torches")
public class HardcoreTorchesConfig implements ConfigData {
    @Comment("Torches will extinguish if broken. Default: false")
    public boolean torchesExtinguishWhenBroken = false;

    @Comment("Torches are fully expended when broken. Overrides torchesExtinguishWhenBroken. Default: false")
    public boolean torchesBurnWhenDropped = false;

    @Comment("Torches become unlit in rain. If torchesSmolder is true, they will instead smolder in rain. Default: true")
    public boolean torchesRain = true;

    @Comment("Only matters if torchesRain is true. In rain, torches will extinguish but emit smoke, and consume fuel at 1/3 the rate until fully extinguished or re-lit. Default: true")
    public boolean torchesSmolder = true;

    @Comment("Burnt torches drop as vanilla stick when broken instead of a burnt torch. Default: false")
    public boolean burntStick = false;

    @Comment("Picking up vanilla torches converts them to lit torches. Default: false")
    public boolean convertVanillaTorches = false;

    @Comment("Picked up vanilla torches will be converted as already lit. Default: false")
    public boolean torchesBurnWhenConverted = false;

    @Comment("The amount of ticks the torch lasts. Default: is 24000. 20 ticks per second, 24000 = 20 minutes")
    public int defaultTorchFuel = 24000;

    @Comment("The amount of ticks the lantern can last. Default: is 24000. 20 ticks per second, 24000 = 20 minutes")
    public int defaultLanternFuel = 24000;

    @Comment("The amount a fuel item adds to the lantern by default. Default: 12000")
    public int defLanternFuelItem = 12000;

    @Comment("A lantern must have at least this much fuel (min 1) to be ignited from unlit. Once lit it will continue to burn to 0. Default: 1")
    public int minLanternIgnitionFuel = 1;

    @Comment("The amount of damage to a torch when it is extinguished (by water, rain, storage, or manually). Default: 800")
    public int torchesExtinguishConditionLoss = 800;

    @Comment("Torches use up more fuel when held by a player vs. being placed (e.g. 2 means burn time is halved). Default: 2")
    public int torchFuelUseMultiplierWhenHeld = 2;

    @Comment("Do torches become unlit when placed in storage. Default: false")
    public boolean unlightInChest = false;

    @Comment("Do torches lose fuel while the player has then in their inventory. Default: true")
    public boolean tickInInventory = true;

    @Comment("How many torches are crafted. Default: 2")
    public int craftAmount = 2;

    @Comment("How many candles are crafted. Default: 4")
    public int candleCraftAmount = 4;

    @Comment("Right click torch or lantern to see fuel value. Default: false")
    public boolean fuelMessage = false;

    @Comment("Max fuel an oil can holds. Default: 576000")
    public int maxCanFuel = 576000;

    @Comment("Do lanterns have to be filled using an oil can. Default: true")
    public boolean lanternsNeedCan = true;

    @Comment("Can you refuel a torch using an oil can. Default: false")
    public boolean torchesUseCan = false;

    @Comment("Multiplies the fuel value of all oil can recipes. 0.5 makes all fuel recipes return half as much fuel. Default: 1")
    public float oilRecipeMultiplier = 1;

    @Comment("Overrides the oil can fuel recipe if set. Default: -1")
    public int oilRecipeOverride = -1;

    @Comment("If true, you can craft animal fat with an oil can to fill it. You can also add custom fill recipes with a datapack, open the mod jar to see the JSON format. Default: true")
    public boolean enableFatOil = true;

    @Comment("If true, you can craft coal with an oil can to fill it. You can also add custom fill recipes with a datapack, open the mod jar to see the JSON format. Default: false")
    public boolean enableCoalOil = false;

    @Comment("If true, you can right click torches to extinguish them while not holding fuel or a torch to light. Default: true")
    public boolean handUnlightTorch = true;

    @Comment("If true, you can right click lanterns to extinguish them while not holding fuel or a torch to light. Default: true")
    public boolean handUnlightLantern = true;

    @Comment("0: When going underwater, torches in your inventory will be unaffected\n1: When going underwater, torches in mainhand or offhand will be extinguished\n2: When going underwater, torches in inventory will be extinguished. Default: 1")
    public int invExtinguishInWater = 1;

    @Comment("0: When in rain, torches in your inventory will be unaffected\n1: When in rain, torches in mainhand or offhand will be extinguished or smolder\n2: When in rain, torches in inventory will be extinguished or smolder. Default: 1")
    public int invExtinguishInRain = 1;

    @Comment("Can the fire starter light campfires. Default: true")
    public boolean starterLightCampfires = true;

    @Comment("Can the fire starter light torches. Default: true")
    public boolean starterLightTorches = true;

    @Comment("Can the fire starter create full-block fires. Default: true")
    public boolean starterStartFires = true;

    @Comment("Can the fire starter light lanterns. Default: true")
    public boolean starterLightLanterns = true;

    @Comment("Percentage chance that the fire starter works. Default: 0.5")
    public float starterSuccessChance = 0.5f;

    @Comment("Default: fuel that a lantern starts with when crafted. Default: 0")
    public int startingLanternFuel = 0;

    @Comment("Allow the player to pick up lanterns with sneak-clicking. Default: true")
    public boolean pickUpLanterns = true;

    @Comment("The fuel that candles start with. Default: 6000")
    public int defaultCandleFuel = 6000;

    @Comment("The fuel that glowstone starts with. Default: 576000")
    public int defaultGlowstoneFuel = 576000;

    @Comment("The fuel that shroomlights start with. Default: 10000")
    public int defaultShroomlightFuel = 10000;

    @Comment("The length of daytime in ticks. May be adjusted if other mods alter the length of days. Default: 13000")
    public int worldDaytimeDuration = 13000;

    @Comment("The length of nighttime in ticks. May be adjusted if other mods alter the length of days. Default: 11000")
    public int worldNighttimeDuration = 11000;
}