package net.qxeii.hardcore_torches.util;

import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.collection.DefaultedList;
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

	public static HashMap<TagKey<Item>, List<ItemStack>> getFireStarterItemsByTag(PlayerInventory inventory) {
		HashMap<TagKey<Item>, List<ItemStack>> items = new HashMap<TagKey<Item>, List<ItemStack>>();
		List<TagKey<Item>> tags = List.of(Mod.UNBREAKING_LIGHTER_ITEMS, Mod.SINGLE_USE_LIGHTER_ITEMS,
				Mod.MULTI_USE_LIGHTER_ITEMS);

		List<ItemStack> allInventoryStacks = List.of();

		allInventoryStacks.addAll(inventory.main);
		allInventoryStacks.addAll(inventory.offHand);

		for (ItemStack stack : allInventoryStacks) {
			for (TagKey<Item> tag : tags) {
				if (!stack.isIn(tag)) {
					continue;
				}

				if (items.containsKey(tag)) {
					items.get(tag).add(stack);
				} else {
					items.put(tag, DefaultedList.of());
					items.get(tag).add(stack);
				}
			}
		}

		// Sort items in map by damage value, highest damage first.

		for (TagKey<Item> tag : tags) {
			if (items.containsKey(tag)) {
				items.get(tag).sort((ItemStack a, ItemStack b) -> {
					return b.getDamage() - a.getDamage();
				});
			}
		}

		return items;
	}

}