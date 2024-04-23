package net.qxeii.hardcore_torches.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.block.AbstractLanternBlock;

public class LanternBlockEntity extends FuelBlockEntity {

	public LanternBlockEntity(BlockPos pos, BlockState state) {
		super(Mod.LANTERN_BLOCK_ENTITY, pos, state);
		setFuel(0);
	}

	public static void tick(World world, BlockPos position, BlockState state, LanternBlockEntity blockEntity) {
		if (world.isClient) {
			return;
		}

		if (!(world.getBlockState(position).getBlock() instanceof AbstractLanternBlock)) {
			return;
		}

		var block = (AbstractLanternBlock) world.getBlockState(position).getBlock();

		if (!block.isLit) {
			return;
		}

		blockEntity.modifyFuel(-1);
		blockEntity.markDirty();

		if (blockEntity.isOutOfFuel()) {
			block.onOutOfFuel(world, position, state, true);
		}
	}
}
