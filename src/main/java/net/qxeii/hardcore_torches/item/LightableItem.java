package net.qxeii.hardcore_torches.item;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.util.InventoryUtils;

public interface LightableItem {

	// Lighting

	default boolean findAndUseLighterItem(PlayerEntity player, Hand hand, boolean heldOnly) {
		ItemStack heldStack = player.getStackInHand(hand);

		// Check if player holds a compatible item, use held item first.

		if (InventoryUtils.canUseAsFireStarter(heldStack)) {
			return useLighterItem(player, heldStack, hand);
		}

		if (heldOnly) {
			Mod.LOGGER.debug("No usable lighter item found in hand (held only lighting).");
			return false;
		}

		// Check if player has a compatible item in inventory.

		var lighterItem = InventoryUtils.getFirstUsableFireStarterItemFromInventory(player.getInventory());

		if (lighterItem == null) {
			Mod.LOGGER.debug("No usable lighter item found in inventory.");
			return false;
		}

		return useLighterItem(player, lighterItem, hand);
	}

	default boolean useLighterItem(PlayerEntity player, ItemStack stack, Hand hand) {
		if (player.isCreative()) {
			Mod.LOGGER.debug("Player is in creative mode, lighter item will not be used.");
			return true;
		}

		return damageLighterItemStack(player, stack, hand);
	}

	private boolean damageLighterItemStack(PlayerEntity player, ItemStack stack, Hand hand) {
		if (stack.isIn(Mod.UNBREAKING_LIGHTER_ITEMS)) {
			Mod.LOGGER.debug("Unbreaking lighter item used, no damage applied.");
			return true;
		}

		if (stack.isIn(Mod.MULTI_USE_LIGHTER_ITEMS)) {
			stack.damage(1, player, forwardedPlayer -> {
				breakLighterItemStack(player, stack, hand);
			});

			Mod.LOGGER.debug("Multi-use lighter item used, damage applied.");
			return true;
		}

		if (stack.isIn(Mod.SINGLE_USE_LIGHTER_ITEMS)) {
			stack.decrement(1);

			Mod.LOGGER.debug("Single-use lighter item used, item consumed in stack.");
			return true;
		}

		Mod.LOGGER.debug("Lighter item is not supported, no damage applied.");
		return false;
	}

	private void breakLighterItemStack(PlayerEntity player, ItemStack lighterStack, Hand torchHand) {
		EquipmentSlot torchHandSlot = torchHand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
		int lighterSlot = player.getInventory().getSlotWithStack(lighterStack);

		// Check if player holds lighter in main or off hand.
		// If found, lighter item can be broken directly without
		// moving items around for equipment break event submission.

		if (player.getMainHandStack() == lighterStack) {
			player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
			breakLighterItemInPlayerInventory(player, lighterSlot);
			return;
		}

		if (player.getOffHandStack() == lighterStack) {
			player.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND);
			breakLighterItemInPlayerInventory(player, lighterSlot);
			return;
		}

		// Regular case, lighter item is in inventory.
		// Must be equipped, event sent, previous item equipped,
		// and ruined lighter item replaced if possible.

		ItemStack torchStack = player.getEquippedStack(torchHandSlot);

		player.equipStack(torchHandSlot, lighterStack);
		player.sendEquipmentBreakStatus(torchHandSlot);
		player.equipStack(torchHandSlot, torchStack);

		breakLighterItemInPlayerInventory(player, lighterSlot);
	}

	private void breakLighterItemInPlayerInventory(PlayerEntity player, int slot) {
		if (!FabricLoader.getInstance().isModLoaded("ruined_equipment")) {
			// If equipment mod is not loaded, item can be removed directly.
			player.getInventory().removeStack(slot);
			return;
		}

		if (player.getInventory().getStack(slot).getItem() != Items.FLINT_AND_STEEL) {
			// Only flint and steel are supported to get auto-replaced
			// with ruined item equivalent.
			return;
		}

		// Get ruined flint and steel from the ruined_equipment mod.

		var ruinedFlintAndSteelId = new Identifier("ruined_equipment", "ruined_flint_and_steel");
		var ruinedFlintAndSteel = Registries.ITEM.get(ruinedFlintAndSteelId);

		// Check if the item is registered.
		if (ruinedFlintAndSteel == Items.AIR) {
			return;
		}

		// Create an ItemStack of the ruined flint and steel.
		ItemStack ruinedFlintAndSteelStack = new ItemStack(ruinedFlintAndSteel, 1);

		// Add ruined flint and steel to player inventory.
		player.getInventory().setStack(slot, ruinedFlintAndSteelStack);
	}

}
