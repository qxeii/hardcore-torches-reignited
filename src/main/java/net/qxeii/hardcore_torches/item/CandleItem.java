package net.qxeii.hardcore_torches.item;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.qxeii.hardcore_torches.Mod;

public class CandleItem extends BlockItem implements FabricItem {

	public CandleItem(Block block, Settings settings) {
		super(block, settings);
	}

	public static ItemStack stateStack(ItemStack inputStack, boolean isLit) {
		ItemStack outputStack = ItemStack.EMPTY;

		if (inputStack.getItem() instanceof BlockItem && inputStack.getItem() instanceof CandleItem) {
			CandleItem newItem = (CandleItem) Mod.CANDLE.asItem();

			outputStack = new ItemStack(newItem, inputStack.getCount());

			if (inputStack.getNbt() != null) {
				outputStack.setNbt(inputStack.getNbt().copy());
			}
		}

		return outputStack;
	}
}
