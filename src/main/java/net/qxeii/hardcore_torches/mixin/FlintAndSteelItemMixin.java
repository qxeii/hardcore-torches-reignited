package net.qxeii.hardcore_torches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelItemMixin {

	@Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
	public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		PlayerEntity playerEntity = context.getPlayer();
		World world = context.getWorld();
		BlockPos blockPos = context.getBlockPos();
		BlockState blockState = world.getBlockState(blockPos);

		if (!CampfireBlock.canBeLit(blockState) && !CandleBlock.canBeLit(blockState)
				&& !CandleCakeBlock.canBeLit(blockState)
				&& !net.qxeii.hardcore_torches.block.CandleBlock.canBeLit(blockState)) {
			BlockPos blockPos2 = blockPos.offset(context.getSide());
			if (AbstractFireBlock.canPlaceAt(world, blockPos2, context.getHorizontalPlayerFacing())) {
				world.playSound(playerEntity, blockPos2, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F,
						world.getRandom().nextFloat() * 0.4F + 0.8F);
				BlockState blockState2 = AbstractFireBlock.getState(world, blockPos2);
				world.setBlockState(blockPos2, blockState2, 11);
				world.emitGameEvent(playerEntity, GameEvent.BLOCK_PLACE, blockPos);
				ItemStack itemStack = context.getStack();
				if (playerEntity instanceof ServerPlayerEntity) {
					Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) playerEntity, blockPos2, itemStack);
					itemStack.damage(1, playerEntity, (p) -> {
						p.sendToolBreakStatus(context.getHand());
					});
				}

				cir.setReturnValue(ActionResult.success(world.isClient));
				return;
			} else {
				cir.setReturnValue(ActionResult.FAIL);
				return;
			}
		} else {
			world.playSound(playerEntity, blockPos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F,
					world.getRandom().nextFloat() * 0.4F + 0.8F);
			world.setBlockState(blockPos, (BlockState) blockState.with(Properties.LIT, true), 11);
			world.emitGameEvent(playerEntity, GameEvent.BLOCK_CHANGE, blockPos);
			if (playerEntity != null) {
				context.getStack().damage(1, playerEntity, (p) -> {
					p.sendToolBreakStatus(context.getHand());
				});
			}

			cir.setReturnValue(ActionResult.success(world.isClient));
			return;
		}
	}

}
