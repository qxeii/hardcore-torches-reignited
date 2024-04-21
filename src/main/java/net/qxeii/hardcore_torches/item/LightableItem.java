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

	default boolean findAndUseLighterItem(PlayerEntity player, ItemStack stack, Hand hand) {
		// Check if player holds a compatible item, use held item first.

		if (InventoryUtils.canUseAsFireStarter(stack)) {
			return useLighterItem(player, stack, hand);
		}

		// Check if player has a compatible item in inventory.

		var lighterItem = InventoryUtils.getFirstUsableFireStarterItemFromInventory(player.getInventory());

		if (lighterItem == null) {
			return false;
		}

		return useLighterItem(player, lighterItem, hand);
	}

	default boolean useLighterItem(PlayerEntity player, ItemStack stack, Hand hand) {
		if (player.isCreative()) {
			return true;
		}

		return damageLighterItemStack(player, stack, hand);
	}

	private boolean damageLighterItemStack(PlayerEntity player, ItemStack stack, Hand hand) {
		if (stack.isIn(Mod.UNBREAKING_LIGHTER_ITEMS)) {
			return true;
		}

		if (stack.isIn(Mod.MULTI_USE_LIGHTER_ITEMS)) {
			stack.damage(1, player, forwardedPlayer -> {
				breakLighterItemStack(player, stack, hand);
			});

			return true;
		}

		if (stack.isIn(Mod.SINGLE_USE_LIGHTER_ITEMS)) {
			stack.decrement(1);
			return true;
		}

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
			addRuinedFlintAndSteelToPlayerInventory(player, lighterSlot);
			return;
		}

		if (player.getOffHandStack() == lighterStack) {
			player.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND);
			addRuinedFlintAndSteelToPlayerInventory(player, lighterSlot);
			return;
		}

		// Regular case, lighter item is in inventory.
		// Must be equipped, event sent, previous item equipped,
		// and ruined lighter item replaced if possible.

		ItemStack torchStack = player.getEquippedStack(torchHandSlot);

		player.equipStack(torchHandSlot, lighterStack);
		player.sendEquipmentBreakStatus(torchHandSlot);
		player.equipStack(torchHandSlot, torchStack);

		addRuinedFlintAndSteelToPlayerInventory(player, lighterSlot);
	}

	private void addRuinedFlintAndSteelToPlayerInventory(PlayerEntity player, int slot) {
		if (!FabricLoader.getInstance().isModLoaded("ruined_equipment")) {
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
