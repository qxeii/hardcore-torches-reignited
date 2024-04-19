package net.qxeii.hardcore_torches.util;

import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

public class Helper {
	public static List<BlockPos> getAdjacent(BlockPos pos) {
		return (Arrays.asList(pos.north(1), pos.south(1), pos.west(1), pos.east(1), pos.up(1), pos.down(1)));
	}
}
