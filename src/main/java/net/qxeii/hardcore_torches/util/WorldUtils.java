package net.qxeii.hardcore_torches.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import net.qxeii.hardcore_torches.Mod;

public class WorldUtils {

	// Time

	public static int getWorldDaytimeLength() {
		return Mod.config.worldDaytimeDuration;
	}

	public static int getWorldNighttimeLength() {
		return Mod.config.worldNighttimeDuration;
	}

	public static int getWorldDayLength() {
		return getWorldDaytimeLength() + getWorldNighttimeLength();
	}

	public static boolean worldIsRaining(World world, BlockEntity entity) {
		return world.hasRain(entity.getPos());
	}

	public static boolean worldIsDaytime(World world) {
		return (world.getTimeOfDay() % getWorldDayLength()) < getWorldDaytimeLength();
	}

	public static boolean worldIsNighttime(World world) {
		return !worldIsDaytime(world);
	}

	// Environment

	public static boolean worldIsNether(World world) {
		return world.getDimensionKey().equals(DimensionTypes.THE_NETHER);
	}

}
