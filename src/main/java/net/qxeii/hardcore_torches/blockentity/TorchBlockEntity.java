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
		fuel = Mod.config.defaultTorchFuel;
	}

	public static void tick(World world, BlockPos pos, BlockState state, TorchBlockEntity be) {
		if (!world.isClient) {
			if (!(state.getBlock() instanceof AbstractTorchBlock))
				return;
			if (((AbstractTorchBlock) state.getBlock()).getBurnState() == ETorchState.LIT) {
				tickLit(world, pos, state, be);
			} else if (((AbstractTorchBlock) state.getBlock()).getBurnState() == ETorchState.SMOLDERING) {
				tickSmoldering(world, pos, state, be);
			}
		}
	}

	private static void tickLit(World world, BlockPos pos, BlockState state, TorchBlockEntity be) {
		AbstractTorchBlock torchBlock = (AbstractTorchBlock) world.getBlockState(pos).getBlock();

		// Extinguish
		if (Mod.config.torchesRain && world.hasRain(pos)) {
			if (random.nextInt(Mod.config.torchesRainAffectTickChance) == 0) {
				if (Mod.config.torchesSmolder) {
					torchBlock.smother(world, pos, state, true);
				} else {
					torchBlock.extinguish(world, pos, state,
							true);
				}
			}

			be.fuel -= Mod.config.torchesExtinguishFuelLoss;
		} else {
			be.fuel--;
		}

		if (be.fuel == 0) {
			((AbstractTorchBlock) world.getBlockState(pos).getBlock()).onOutOfFuel(world, pos, state, false);
		}

		be.markDirty();
	}

	private static void tickSmoldering(World world, BlockPos pos, BlockState state, TorchBlockEntity be) {

		// Burn out
		if (random.nextInt(Mod.config.torchesSmolderFuelUseTickChance) == 0) {
			be.fuel--;

			if (be.fuel == 0) {
				((AbstractTorchBlock) world.getBlockState(pos).getBlock()).burnOut(world, pos, state,
						false);
			}
		} else if (random.nextInt(Mod.config.torchesSmolderExtinguishTickChance) == 0) {
			((AbstractTorchBlock) world.getBlockState(pos).getBlock()).extinguish(world, pos, state, false);
		}

		be.markDirty();
	}
}
