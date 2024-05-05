package net.qxeii.hardcore_torches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.mixinlogic.CampfireBlockMixinLogic;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin implements CampfireBlockMixinLogic {

	@Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
	private void getPlacementState(ItemPlacementContext context,
			CallbackInfoReturnable<BlockState> callbackInfo) {
		callbackInfo.setReturnValue(injectedGetPlacementState(context, callbackInfo.getReturnValue()));
	}

	@Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
	public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit,
			CallbackInfoReturnable<ActionResult> callbackInfo) {
		var useActionResult = injectedOnUse(state, world, pos, player, hand, hit);
		callbackInfo.setReturnValue(useActionResult);
	}

}
