package net.qxeii.hardcore_torches.item;

import static net.minecraft.util.math.MathHelper.clamp;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.util.WorldUtils;

public class LanternItem extends BlockItem {

	public int maxFuel;
	public boolean isLit;

	public LanternItem(Block block, Settings settings, int maxFuel, boolean isLit) {
		super(block, settings);
		this.maxFuel = maxFuel;
		this.isLit = isLit;
	}

	// Properties

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		int fuel = getFuel(stack);

		if (fuel < maxFuel) {
			return true;
		}

		return false;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		int fuel = getFuel(stack);

		if (maxFuel != 0) {
			return Math.round(13.0f - (maxFuel - fuel) * 13.0f / maxFuel);
		}

		return 0;
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		return MathHelper.hsvToRgb(3.0f, 1.0f, 1.0f);
	}

	public static int getFuel(ItemStack stack) {
		var nbt = stack.getNbt();
		var isLit = ((LanternItem) stack.getItem()).isLit;

		if (nbt == null) {
			if (isLit) {
				return Mod.config.defaultLanternFuel;
			}

			return Mod.config.startingLanternFuel;
		}

		return clamp(nbt.getInt("Fuel"), 0, Mod.config.defaultLanternFuel);
	}

	@Override
	public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
		NbtCompound oldNbt = null;
		NbtCompound newNbt = null;

		if (oldStack.getNbt() != null) {
			oldNbt = oldStack.getNbt().copy();
			oldNbt.remove("Fuel");
		}

		if (newStack.getNbt() != null) {
			newNbt = newStack.getNbt().copy();
			newNbt.remove("Fuel");
		}

		if (oldNbt == null && newNbt != null)
			return true;
		if (oldNbt != null && newNbt == null)
			return true;
		if (oldNbt == null && newNbt == null)
			return false;

		return oldNbt.equals(null);
	}

	// Interaction

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (world.isClient) {
			return super.use(world, player, hand);
		}

		if (player.isSneaking()) {
			if (Mod.config.fuelMessage) {
				displayFuelMessage(world, player, player.getStackInHand(hand));
			}

			return super.use(world, player, hand);
		}

		if (isLit) {
			extinguishWithInteraction(world, player, hand);
		} else {
			lightWithInteraction(world, player, hand);
		}

		return super.use(world, player, hand);
	}

	public void extinguishWithInteraction(World world, PlayerEntity player, Hand hand) {
		PlayerInventory inventory = player.getInventory();
		int slot = hand == Hand.MAIN_HAND ? inventory.selectedSlot : PlayerInventory.OFF_HAND_SLOT;

		extinguish(world, player, slot);
		player.swingHand(hand);
	}

	public void lightWithInteraction(World world, PlayerEntity player, Hand hand) {
		PlayerInventory inventory = player.getInventory();
		int slot = hand == Hand.MAIN_HAND ? inventory.selectedSlot : PlayerInventory.OFF_HAND_SLOT;

		light(world, player, slot);
		player.swingHand(hand);

		if (Mod.config.fuelMessage) {
			displayFuelMessage(world, player, player.getStackInHand(hand));
		}
	}

	private void displayFuelMessage(World world, PlayerEntity player, ItemStack stack) {
		var fuel = getFuel(stack);
		player.sendMessage(WorldUtils.formattedFuelText(fuel), true);
	}

	// Actions

	public void extinguish(World world, PlayerEntity player, int slot) {
		PlayerInventory inventory = player.getInventory();
		ItemStack stack = inventory.getStack(slot);

		if (stack.getItem() instanceof LanternItem) {
			stack = modifiedStackWithState(world, stack, false);
			inventory.setStack(slot, stack);
		}

		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f,
				1.0f);
	}

	public void light(World world, PlayerEntity player, int slot) {
		PlayerInventory inventory = player.getInventory();
		ItemStack stack = inventory.getStack(slot);

		stack = modifiedStackWithAddedFuel(world, stack, -Mod.config.lanternLightFuelLoss);

		if (getFuel(stack) < Mod.config.minLanternIgnitionFuel) {
			// Lantern fuel is depleted, do not light and bail.
			world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0f, 2.0f);
			return;
		}

		if (stack.getItem() instanceof LanternItem) {
			stack = modifiedStackWithState(world, stack, true);
			inventory.setStack(slot, stack);
		}

		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 0.5f,
				1.0f);
	}

	// Fuel

	public static ItemStack modifiedStackWithAddedFuel(World world, ItemStack stack, int amount) {
		if (world.isClient) {
			return stack;
		}

		if (!(stack.getItem() instanceof LanternItem)) {
			return stack;
		}

		LanternItem item = (LanternItem) stack.getItem();

		NbtCompound nbt = stack.getNbt();
		int fuel = item.isLit ? item.maxFuel : 0;

		if (nbt != null) {
			fuel = nbt.getInt("Fuel");
		} else {
			nbt = new NbtCompound();
		}

		fuel += amount;

		if (fuel <= 0) {
			return modifiedStackWithState(world, stack, false);
		}

		if (fuel > Mod.config.defaultLanternFuel) {
			fuel = Mod.config.defaultLanternFuel;
		}

		nbt.putInt("Fuel", fuel);
		stack.setNbt(nbt);

		return stack;
	}

	// Modification

	public static ItemStack modifiedStackWithState(World world, ItemStack stack, boolean isLit) {
		if (world.isClient) {
			return stack;
		}

		if (!(stack.getItem() instanceof LanternItem)) {
			return ItemStack.EMPTY;
		}

		LanternItem newItem = (LanternItem) (isLit ? Mod.LIT_LANTERN.asItem() : Mod.UNLIT_LANTERN.asItem());
		var modifiedStack = new ItemStack(newItem, stack.getCount());

		if (stack.getNbt() != null) {
			modifiedStack.setNbt(stack.getNbt().copy());
		}

		return modifiedStack;
	}

	// Tooltips

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		// tooltip.add(MutableText.of(new LiteralTextContent("Light with Flint and
		// Steel")).formatted(Formatting.GRAY));
		super.appendTooltip(stack, world, tooltip, context);
	}

}
