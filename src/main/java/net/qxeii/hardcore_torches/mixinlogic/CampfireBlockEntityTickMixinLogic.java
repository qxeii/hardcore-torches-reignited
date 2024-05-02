package net.qxeii.hardcore_torches.mixinlogic;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.util.WorldUtils;

public interface CampfireBlockEntityTickMixinLogic {

	public static final String NBT_KEY_FUEL = "Fuel";

	public int getFuel();

	public void setFuel(int fuel);

	public boolean isOutOfFuel();

	public default void injectedWriteNbt(NbtCompound nbt) {
		nbt.putInt(NBT_KEY_FUEL, this.getFuel());
	}

	public default void injectedRead(NbtCompound nbt) {
		if (!nbt.contains(NBT_KEY_FUEL)) {
			return;
		}

		var fuel = nbt.getInt(NBT_KEY_FUEL);
		setFuel(fuel);
	}

	public static void litClientTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
		var campfireMixin = (CampfireBlockEntityTickMixinLogic) (Object) campfire;

		if (campfireMixin.isOutOfFuel()) {
			extinguish(world, pos, state);
			return;
		}
	}

	public static void litServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
		var campfireMixin = (CampfireBlockEntityTickMixinLogic) (Object) campfire;

		if (campfireMixin.isOutOfFuel()) {
			extinguish(world, pos, state);
			return;
		}

		if (WorldUtils.worldIsRaining(world, campfire)) {
			if (world.random.nextInt(Mod.config.campfiresRainAffectTickChance) == 0) {
				extinguish(world, pos, state);
				return;
			}
		}

		campfireMixin.setFuel(campfireMixin.getFuel() - 1);
	}

	private static void extinguish(World world, BlockPos pos, BlockState state) {
		CampfireBlock.extinguish(null, world, pos, state);

		if (!world.isClient()) {
			world.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS,
					1.0F, 1.0F);

			world.setBlockState(pos, state.with(CampfireBlock.LIT, false), 3);
		}
	}

}
