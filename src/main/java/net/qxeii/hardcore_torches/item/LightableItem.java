package net.qxeii.hardcore_torches.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.util.InventoryUtils;

public interface LightableItem {

	// Lighting

	default boolean findAndUseLighterItem(PlayerEntity player, ItemStack stack, Hand hand) {
		// Check if player holds a compatible item, use held item first.

		if (InventoryUtils.canUseAsFireStarter(stack)) {
			return useLighterItem(player, stack);
		}

		// Check if player has a compatible item in inventory.

		var lighterItem = InventoryUtils.getFirstUsableFireStarterItemFromInventory(player.getInventory());

		if (lighterItem == null) {
			return false;
		}

		return useLighterItem(player, lighterItem);
	}

	default boolean useLighterItem(PlayerEntity player, ItemStack stack) {
		if (player.isCreative()) {
			return true;
		}

		if (stack.isIn(Mod.UNBREAKING_LIGHTER_ITEMS)) {
			return true;
		}

		if (stack.isIn(Mod.MULTI_USE_LIGHTER_ITEMS)) {
			stack.damage(1, player, p -> p.sendToolBreakStatus(Hand.MAIN_HAND));
			return true;
		}

		if (stack.isIn(Mod.SINGLE_USE_LIGHTER_ITEMS)) {
			stack.decrement(1);
			return true;
		}

		return false;
	}

}
