package net.qxeii.hardcore_torches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.item.ItemPlacementContext;
import net.qxeii.hardcore_torches.mixinlogic.CampfireBlockMixinLogic;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin implements CampfireBlockMixinLogic {

	@Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
	private void getPlacementState(ItemPlacementContext context,
			CallbackInfoReturnable<BlockState> callbackInfo) {
		callbackInfo.setReturnValue(injectedGetPlacementState(context, callbackInfo.getReturnValue()));
	}

}
