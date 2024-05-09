package net.qxeii.hardcore_torches.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.block.AbstractTorchBlock;
import net.qxeii.hardcore_torches.util.ETorchState;

public class TorchBlockEntity extends FuelBlockEntity {

	public TorchBlockEntity(BlockPos pos, BlockState state) {
		super(Mod.TORCH_BLOCK_ENTITY, pos, state);
		setFuel(0);
	}

	public static void tick(World world, BlockPos position, BlockState state, TorchBlockEntity blockEntity) {
		if (world.isClient) {
			return;
		}

		if (!(state.getBlock() instanceof AbstractTorchBlock)) {
			return;
		}

		var block = (AbstractTorchBlock) state.getBlock();

		if (block.getBurnState() == ETorchState.LIT) {
			tickLit(world, position, state, blockEntity);
		} else if (block.getBurnState() == ETorchState.SMOLDERING) {
			tickSmoldering(world, position, state, blockEntity);
		}
	}

	private static void tickLit(World world, BlockPos position, BlockState state, TorchBlockEntity blockEntity) {
		AbstractTorchBlock torchBlock = (AbstractTorchBlock) world.getBlockState(position).getBlock();

		// Extinguish
		if (Mod.config.torchesRain && world.hasRain(position)) {
			if (random.nextInt(Mod.config.torchesRainAffectTickChance) == 0) {
				if (Mod.config.torchesSmolder) {
					torchBlock.smother(world, position, state, true);
				} else {
					torchBlock.extinguish(world, position, state,
							true);
				}
			}

			blockEntity.modifyFuel(-Mod.config.torchesExtinguishFuelLoss);
		} else {
			blockEntity.modifyFuel(-1);
		}

		if (blockEntity.isOutOfFuel()) {
			((AbstractTorchBlock) world.getBlockState(position).getBlock()).onOutOfFuel(world, position, state, false);
		}

		blockEntity.markDirty();
	}

	private static void tickSmoldering(World world, BlockPos position, BlockState state, TorchBlockEntity blockEntity) {

		// Burn out
		if (random.nextInt(Mod.config.torchesSmolderFuelUseTickChance) == 0) {
			blockEntity.modifyFuel(-1);

			if (blockEntity.isOutOfFuel()) {
				((AbstractTorchBlock) world.getBlockState(position).getBlock()).onOutOfFuel(world, position, state,
						false);
			}
		} else if (random.nextInt(Mod.config.torchesSmolderExtinguishTickChance) == 0) {
			((AbstractTorchBlock) world.getBlockState(position).getBlock()).extinguish(world, position, state, false);
		}

		blockEntity.markDirty();
	}
}
