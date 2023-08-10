package net.qxeii.hardcore_torches.item;

import net.qxeii.hardcore_torches.Mod;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.block.Block;
import net.minecraft.block.CandleBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
