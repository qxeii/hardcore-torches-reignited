package net.qxeii.hardcore_torches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.mixinlogic.CampfireBlockEntityTickMixinLogic;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin implements CampfireBlockEntityTickMixinLogic {

	// Properties

	@Unique
	public int fuel = Mod.config.defaultCampfireFuel;

	@Unique
	public int getFuel() {
		return fuel;
	}

	@Unique
	public void setFuel(int fuel) {
		if (fuel < 0) {
			fuel = 0;
		}

		this.fuel = fuel;
	}

	@Unique
	public boolean isOutOfFuel() {
		return fuel == 0;

	}

	// NBT

	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void writeNbt(NbtCompound nbt, CallbackInfo callbackInfo) {
		injectedWriteNbt(nbt);
	}

	@Inject(method = "readNbt", at = @At("TAIL"))
	private void readNbt(NbtCompound nbt, CallbackInfo callbackInfo) {
		injectedRead(nbt);
	}

	// Tick

	@Inject(method = "clientTick", at = @At("TAIL"))
	private static void injectedClientTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire,
			CallbackInfo callbackInfo) {
		var isLit = state.get(CampfireBlock.LIT);

		if (!isLit) {
			return;
		}

		CampfireBlockEntityTickMixinLogic.litClientTick(world, pos, state, campfire);
	}

	@Inject(method = "litServerTick", at = @At("TAIL"))
	private static void injectedLitServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire,
			CallbackInfo callbackInfo) {
		CampfireBlockEntityTickMixinLogic.litServerTick(world, pos, state, campfire);
	}

}
