package net.qxeii.hardcore_torches.mixinlogic;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.util.WorldUtils;

public interface CampfireBlockEntityMixinLogic {

	public static final String NBT_KEY_FUEL = "Fuel";

	public int getFuel();

	public void setFuel(int fuel);

	public boolean isOutOfFuel();

	public default void injectedWriteNbt(NbtCompound nbt) {
		nbt.putInt(NBT_KEY_FUEL, this.getFuel());
	}

	public default void injectedReadNbt(NbtCompound nbt) {
		if (nbt.contains(NBT_KEY_FUEL)) {
			var fuel = nbt.getInt(NBT_KEY_FUEL);
			setFuel(fuel);
		}
	}

	public default void injectedInitialChunkDataNbt(NbtCompound nbt) {
		nbt.putInt(NBT_KEY_FUEL, this.getFuel());
	}

	public static void litClientTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity _campfire) {
		var campfire = (CampfireBlockEntityMixinLogic) world.getBlockEntity(pos);

		if (campfire == null) {
			return;
		}

		if (campfire.isOutOfFuel()) {
			extinguish(world, pos, state);
			return;
		}

		if (WorldUtils.worldIsRaining(world, (BlockEntity) campfire)) {
			if (world.random.nextInt(Mod.config.campfiresRainAffectTickChance) == 0) {
				extinguish(world, pos, state);
				markDirty(world, pos, state);
				return;
			}
		}
	}

	public static void litServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity _campfire) {
		var campfire = (CampfireBlockEntityMixinLogic) world.getBlockEntity(pos);

		if (campfire == null) {
			return;
		}

		if (campfire.isOutOfFuel()) {
			extinguish(world, pos, state);
			markDirty(world, pos, state);
			return;
		}

		if (WorldUtils.worldIsRaining(world, (BlockEntity) campfire)) {
			if (world.random.nextInt(Mod.config.campfiresRainAffectTickChance) == 0) {
				extinguish(world, pos, state);
				markDirty(world, pos, state);
				return;
			}
		}

		campfire.setFuel(campfire.getFuel() - 1);
		markDirty(world, pos, state);
	}

	public static void extinguish(World world, BlockPos pos, BlockState state) {
		var campfire = (CampfireBlockEntityMixinLogic) world.getBlockEntity(pos);

		CampfireBlock.extinguish(null, world, pos, state);
		campfire.setFuel(campfire.getFuel() - Mod.config.campfireExtinguishFuelLoss);

		if (!world.isClient) {
			world.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS,
					0.8F, 0.6F);

			world.setBlockState(pos, state.with(CampfireBlock.LIT, false), 3);
		}
	}

	private static void markDirty(World world, BlockPos position, BlockState state) {
		world.markDirty(position);

		if (!state.isAir()) {
			world.updateComparators(position, state.getBlock());
		}
	}

}
