package net.qxeii.hardcore_torches.blockentity;

import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.floor;
import static net.qxeii.hardcore_torches.util.Helper.getAdjacent;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.block.AbstractShroomlightBlock;
import net.qxeii.hardcore_torches.util.WorldUtils;

public class ShroomlightBlockEntity extends FuelBlockEntity {

	public ShroomlightBlockEntity(BlockPos pos, BlockState state) {
		super(Mod.SHROOMLIGHT_BLOCK_ENTITY, pos, state);
		setFuel(0);
	}

	public static void tick(World world, BlockPos position, BlockState state, ShroomlightBlockEntity blockEntity) {
		// Burn out
		if (world.isClient) {
			return;
		}

		if (!(world.getBlockState(position).getBlock() instanceof AbstractShroomlightBlock)) {
			return;
		}

		double fuelRatio = ((double) blockEntity.getFuel() / (double) Mod.config.defaultShroomlightFuel);
		int lightPower = floor((fuelRatio * 15.0D));

		if (state.get(Properties.LEVEL_15) != lightPower) {
			BlockState newState = state.with(Properties.LEVEL_15, lightPower);
			world.setBlockState(position, newState);
		}

		if (world.getDimensionKey() == DimensionTypes.THE_NETHER
				&& blockEntity.getFuel() < Mod.config.defaultShroomlightFuel) {
			// Charge shroomlight in the nether.
			blockEntity.modifyFuel(1);
		} else {
			if (world.getDimensionKey() == DimensionTypes.OVERWORLD
					&& (world.getTimeOfDay() % WorldUtils.getWorldDayLength()) < WorldUtils.getWorldDaytimeLength() &&
					(getAdjacent(position).stream().mapToInt(x -> world.getLightLevel(LightType.SKY, x)).sum()
							/ 6) > 0) {
				// Honestly, I have no idea what is even happening here.
				blockEntity.setFuel(clamp(
						blockEntity.getFuel() + (getAdjacent(position).stream()
								.mapToInt(value -> world.getLightLevel(LightType.SKY, value)).sum() / 6),
						0, Mod.config.defaultShroomlightFuel));
			} else {
				blockEntity.modifyFuel(-1);
			}
		}

		blockEntity.markDirty();
	}
}