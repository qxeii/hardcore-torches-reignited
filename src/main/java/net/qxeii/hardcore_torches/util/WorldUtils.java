package net.qxeii.hardcore_torches.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
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
		return world.hasRain(entity.getPos().offset(Direction.UP, 1));
	}

	public static boolean worldIsDaytime(World world) {
		return (world.getTimeOfDay() % getWorldDayLength()) < getWorldDaytimeLength();
	}

	public static boolean worldIsNighttime(World world) {
		return !worldIsDaytime(world);
	}

	public static Text formattedFuelText(int fuel) {
		return Text.translatable("text.hardcore_torches.fuel_message", formattedFuelTime(fuel));
	}

	public static Text formattedFuelTime(int fuel) {
		int seconds = fuel / 20;
		int minutes = seconds / 60;

		if (minutes > 1) {
			return Text.translatable("text.hardcore_torches.fuel_time_minutes", minutes);
		}

		if (seconds == 1) {
			return Text.translatable("text.hardcore_torches.fuel_time_second", seconds);
		}

		return Text.translatable("text.hardcore_torches.fuel_time_seconds", seconds);
	}

	// Environment

	public static boolean worldIsNether(World world) {
		return world.getDimensionKey().equals(DimensionTypes.THE_NETHER);
	}

}
