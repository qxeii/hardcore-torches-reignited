package net.qxeii.hardcore_torches.util;

import net.minecraft.server.world.ServerWorld;
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

	public static boolean worldIsDaytime(ServerWorld world) {
		return (world.getTimeOfDay() % getWorldDayLength()) < getWorldDaytimeLength();
	}

	public static boolean worldIsNighttime(ServerWorld world) {
		return !worldIsDaytime(world);
	}

	// Environment

	public static boolean worldIsNether(ServerWorld world) {
		return world.getDimensionKey().equals(DimensionTypes.THE_NETHER);
	}

}
