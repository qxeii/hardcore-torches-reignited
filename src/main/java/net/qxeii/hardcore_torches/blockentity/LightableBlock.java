package net.qxeii.hardcore_torches.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.item.LightableItem;

public interface LightableBlock extends LightableItem {

	// Events

	void onOutOfFuel(World world, BlockPos pos, BlockState state, boolean playSound);

}
