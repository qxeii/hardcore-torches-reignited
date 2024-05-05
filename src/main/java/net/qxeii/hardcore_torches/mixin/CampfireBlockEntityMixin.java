package net.qxeii.hardcore_torches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.mixinlogic.CampfireBlockEntityMixinLogic;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin implements CampfireBlockEntityMixinLogic {

	// Properties

	@Unique
	private int fuel = Mod.config.defaultCampfireFuel;

	@Unique
	public int getFuel() {
		return fuel;
	}

	@Unique
	public void setFuel(int fuel) {
		this.fuel = Math.max(0, fuel);
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
		injectedReadNbt(nbt);
	}

	@Inject(method = "toInitialChunkDataNbt", at = @At("TAIL"))
	private void toInitialChunkDataNbt(CallbackInfoReturnable<NbtCompound> callbackInfo) {
		NbtCompound nbtCompound = callbackInfo.getReturnValue();
		injectedInitialChunkDataNbt(nbtCompound);
	}

	// Tick

	@Inject(method = "clientTick", at = @At("TAIL"))
	private static void injectedClientTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire,
			CallbackInfo callbackInfo) {
		if (campfire == null) {
			return;
		}

		var isLit = state.get(CampfireBlock.LIT);

		if (!isLit) {
			return;
		}

		CampfireBlockEntityMixinLogic.litClientTick(world, pos, state, campfire);
	}

	@Inject(method = "litServerTick", at = @At("TAIL"))
	private static void injectedLitServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire,
			CallbackInfo callbackInfo) {
		CampfireBlockEntityMixinLogic.litServerTick(world, pos, state, campfire);
	}

}
