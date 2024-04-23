package net.qxeii.hardcore_torches.blockentity;

import static net.minecraft.util.math.MathHelper.floor;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.block.AbstractGlowstoneBlock;

public class GlowstoneBlockEntity extends FuelBlockEntity {

	public GlowstoneBlockEntity(BlockPos pos, BlockState state) {
		super(Mod.GLOWSTONE_BLOCK_ENTITY, pos, state);
		setFuel(Mod.config.defaultGlowstoneFuel);
	}

	public static void tick(World world, BlockPos position, BlockState state, GlowstoneBlockEntity blockEntity) {
		// Burn out
		if (world.isClient) {
			return;
		}

		if (world.getBlockState(position).getBlock() instanceof AbstractGlowstoneBlock) {
			double fuelRatio = ((double) blockEntity.getFuel() / (double) Mod.config.defaultGlowstoneFuel);
			int lightPower = floor((fuelRatio * 15.0D));

			if (state.get(Properties.LEVEL_15) != lightPower) {
				BlockState newState = state.with(Properties.LEVEL_15, lightPower);
				world.setBlockState(position, newState);
			}

			if (world.getDimensionKey() == DimensionTypes.THE_NETHER) {
				if (blockEntity.getFuel() < Mod.config.defaultGlowstoneFuel) {
					blockEntity.modifyFuel(1);
				}
			} else {
				blockEntity.modifyFuel(-1);
			}

			blockEntity.markDirty();
		}
	}
}