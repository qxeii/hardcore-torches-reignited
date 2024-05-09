package net.qxeii.hardcore_torches.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.qxeii.hardcore_torches.Mod;

public class InventoryUtils {

	// Item Type Check

	public static boolean canUseAsFireStarter(ItemStack stack) {
		var state = stack.isIn(Mod.UNBREAKING_LIGHTER_ITEMS) || stack.isIn(Mod.MULTI_USE_LIGHTER_ITEMS)
				|| stack.isIn(Mod.SINGLE_USE_LIGHTER_ITEMS);

		Mod.LOGGER.debug("Inventory utils reports use of {} as fire starter: {}", stack.getName(), state);

		return state;
	}

	public static boolean canUseAsFuelSource(ItemStack stack) {
		return stack.getItem() == Mod.FUEL_CAN;
	}

	// Items from Inventory

	public static ItemStack getFirstUsableFireStarterItemFromInventory(PlayerInventory inventory) {
		var map = getFireStarterItemsMap(inventory);
		return getFirstUsableFireStarterItemFromMap(map);
	}

	public static ItemStack getFirstUsableFireStarterItemFromMap(FireStarterItemMap map) {
		if (!map.unbreaking.isEmpty()) {
			Mod.LOGGER.debug("Returning {} as first unbreaking fire starter item from map.",
					map.unbreaking.get(0).getName());
			return map.unbreaking.get(0);
		}

		if (!map.multiUse.isEmpty()) {
			Mod.LOGGER.debug("Returning {} as first multi-use fire starter item from map.",
					map.multiUse.get(0).getName());
			return map.multiUse.get(0);
		}

		if (!map.singleUse.isEmpty()) {
			Mod.LOGGER.debug("Returning {} as first single-use fire starter item from map.",
					map.singleUse.get(0).getName());
			return map.singleUse.get(0);
		}

		return null;
	}

	public static FireStarterItemMap getFireStarterItemsMap(PlayerInventory inventory) {
		var map = FireStarterItemMap.of();

		inventory.main.forEach(stack -> map.addStackIfMatching(stack));
		inventory.offHand.forEach(stack -> map.addStackIfMatching(stack));

		map.sortAllListsByDamageValue();

		Mod.LOGGER.debug("Created fire starter item map with {} unbreaking, {} single-use, {} multi-use items.",
				map.unbreaking.size(), map.singleUse.size(), map.multiUse.size());

		return map;
	}

	public static class FireStarterItemMap {
		public List<ItemStack> unbreaking;
		public List<ItemStack> singleUse;
		public List<ItemStack> multiUse;

		public FireStarterItemMap(List<ItemStack> unbreaking, List<ItemStack> singleUse, List<ItemStack> multiUse) {
			this.unbreaking = unbreaking;
			this.singleUse = singleUse;
			this.multiUse = multiUse;
		}

		public static FireStarterItemMap of() {
			return new FireStarterItemMap(
					new ArrayList<ItemStack>(),
					new ArrayList<ItemStack>(),
					new ArrayList<ItemStack>());
		}

		public void addStackIfMatching(ItemStack stack) {
			if (stack.isEmpty()) {
				return;
			}

			if (stack.isIn(Mod.UNBREAKING_LIGHTER_ITEMS)) {
				unbreaking.add(stack);
			} else if (stack.isIn(Mod.SINGLE_USE_LIGHTER_ITEMS)) {
				singleUse.add(stack);
			} else if (stack.isIn(Mod.MULTI_USE_LIGHTER_ITEMS) && stack.getDamage() < stack.getMaxDamage()) {
				multiUse.add(stack);
			}
		}

		public void sortAllListsByDamageValue() {
			unbreaking.sort((ItemStack a, ItemStack b) -> {
				return b.getDamage() - a.getDamage();
			});

			singleUse.sort((ItemStack a, ItemStack b) -> {
				return b.getDamage() - a.getDamage();
			});

			multiUse.sort((ItemStack a, ItemStack b) -> {
				return b.getDamage() - a.getDamage();
			});
		}
	}

}