package net.qxeii.hardcore_torches.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.qxeii.hardcore_torches.Mod;

public class InventoryUtils {

	// Item Type Check

	public static boolean canUseAsFireStarter(ItemStack stack) {
		return stack.isIn(Mod.UNBREAKING_LIGHTER_ITEMS) || stack.isIn(Mod.MULTI_USE_LIGHTER_ITEMS)
				|| stack.isIn(Mod.SINGLE_USE_LIGHTER_ITEMS);
	}

	public static boolean canUseAsFuelSource(ItemStack stack) {
		return stack.getItem() == Mod.OIL_CAN;
	}

	// Items from Inventory

	public static ItemStack getFirstUsableFireStarterItemFromInventory(PlayerInventory inventory) {
		var map = getFireStarterItemsMap(inventory);
		return getFirstUsableFireStarterItemFromMap(map);
	}

	public static ItemStack getFirstUsableFireStarterItemFromMap(FireStarterItemMap map) {
		if (!map.unbreaking.isEmpty()) {
			return map.unbreaking.get(0);
		}

		if (!map.singleUse.isEmpty()) {
			return map.singleUse.get(0);
		}

		if (!map.multiUse.isEmpty()) {
			return map.multiUse.get(0);
		}

		return null;
	}

	public static FireStarterItemMap getFireStarterItemsMap(PlayerInventory inventory) {
		var map = FireStarterItemMap.of();

		List<ItemStack> allInventoryStacks = List.of();

		allInventoryStacks.addAll(inventory.main);
		allInventoryStacks.addAll(inventory.offHand);

		for (ItemStack stack : allInventoryStacks) {
			map.addStackIfMatching(stack);
		}

		map.sortAllListsByDamageValue();
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