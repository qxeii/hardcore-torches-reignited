package net.qxeii.hardcore_torches.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.block.AbstractCandleBlock;

public class CandleBlockEntity extends FuelBlockEntity {

	public CandleBlockEntity(BlockPos pos, BlockState state) {
		super(Mod.CANDLE_BLOCK_ENTITY, pos, state);
		setFuel(Mod.config.defaultCandleFuel);
	}

	public static void tick(World world, BlockPos position, BlockState state, CandleBlockEntity blockEntity) {
		if (world.isClient) {
			return;
		}

		if (!(world.getBlockState(position).getBlock() instanceof AbstractCandleBlock)) {
			return;
		}

		var block = (AbstractCandleBlock) world.getBlockState(position).getBlock();

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
