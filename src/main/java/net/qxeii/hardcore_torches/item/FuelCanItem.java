package net.qxeii.hardcore_torches.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.blockentity.FuelBlockEntity;
import net.qxeii.hardcore_torches.blockentity.LanternBlockEntity;
import net.qxeii.hardcore_torches.blockentity.TorchBlockEntity;

public class FuelCanItem extends Item {

	public FuelCanItem(Settings settings) {
		super(settings);
	}

	// region Fuel Bar
	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		int maxFuel = Mod.config.maxCanFuel;
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
	// endregion

	// region Fuel Methods
	public static int getFuel(ItemStack stack) {
		Item item = stack.getItem();
		if (!(item instanceof FuelCanItem))
			return 0;

		NbtCompound nbt = stack.getNbt();

		if (nbt != null && nbt.contains("Fuel")) {
			return nbt.getInt("Fuel");
		}

		return 0;
	}

	public static ItemStack setFuel(ItemStack stack, int fuel) {
		if (stack.getItem() instanceof FuelCanItem) {
			NbtCompound nbt = stack.getNbt();

			if (nbt == null)
				nbt = new NbtCompound();

			nbt.putInt("Fuel", Math.max(0, Math.min(Mod.config.maxCanFuel, fuel)));
			stack.setNbt(nbt);
		}

		return stack;
	}

	public static ItemStack addFuel(ItemStack stack, int amount) {

		if (stack.getItem() instanceof FuelCanItem) {
			NbtCompound nbt = stack.getNbt();
			int fuel = 0;

			if (nbt != null) {
				fuel = nbt.getInt("Fuel");
			} else {
				nbt = new NbtCompound();
			}

			fuel = Math.min(Mod.config.maxCanFuel, Math.max(0, fuel + amount));

			nbt.putInt("Fuel", fuel);
			stack.setNbt(nbt);
		}

		return stack;
	}

	public static boolean fuelBlock(FuelBlockEntity blockEntity, World world, ItemStack stack) {
		if (world.isClient) {
			return false;
		}

		int maxFuel = transferrableFuelCapacityForEntity(blockEntity);
		int currentFuel = blockEntity.getFuel();
		int transferrableFuel = Math.min(maxFuel, getFuel(stack));

		addFuel(stack, -transferrableFuel);
		blockEntity.setFuel(currentFuel + transferrableFuel);

		return transferrableFuel > 0;
	}

	private static int transferrableFuelCapacityForEntity(FuelBlockEntity blockEntity) {
		int currentFuel = blockEntity.getFuel();

		// Lanterns
		if (blockEntity instanceof LanternBlockEntity) {
			return Math.max(0, Mod.config.defaultLanternFuel - currentFuel);
		}

		// Torches
		if (blockEntity instanceof TorchBlockEntity) {
			return Math.max(0, Mod.config.defaultTorchFuel - currentFuel);
		}

		return 0;
	}
}
