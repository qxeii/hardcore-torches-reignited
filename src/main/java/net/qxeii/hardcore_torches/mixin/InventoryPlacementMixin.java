package net.qxeii.hardcore_torches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.item.TorchItem;
import net.qxeii.hardcore_torches.util.ETorchState;

@Mixin(LootableContainerBlockEntity.class)
public abstract class InventoryPlacementMixin {

	@Shadow
	protected abstract DefaultedList<ItemStack> getInvStackList();

	@Inject(at = @At("TAIL"), method = "setStack")
	private void setStack(int slot, ItemStack stack, CallbackInfo info) {
		if (Mod.config.unlightInChest) {
			LootableContainerBlockEntity container = (LootableContainerBlockEntity) (Object) this;
			World world = container.getWorld();
			Item item = stack.getItem();

			if (item instanceof TorchItem) {
				if (((TorchItem) item).getTorchState() == ETorchState.LIT
						|| ((TorchItem) item).getTorchState() == ETorchState.SMOLDERING) {
					this.getInvStackList().set(slot, TorchItem.modifiedStackWithState(world, stack, ETorchState.UNLIT));
				}
			}
		}
	}
}
