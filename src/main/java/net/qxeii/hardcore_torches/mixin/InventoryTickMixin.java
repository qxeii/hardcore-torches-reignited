package net.qxeii.hardcore_torches.mixin;

import java.util.Random;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.item.GlowstoneItem;
import net.qxeii.hardcore_torches.item.LanternItem;
import net.qxeii.hardcore_torches.item.ShroomlightItem;
import net.qxeii.hardcore_torches.item.TorchItem;
import net.qxeii.hardcore_torches.util.ETorchState;
import net.qxeii.hardcore_torches.util.WorldUtils;

@Mixin(ServerPlayerEntity.class)
public abstract class InventoryTickMixin {
	@Shadow
	public abstract ServerWorld getServerWorld();

	@Shadow
	@Nullable
	private Entity cameraEntity;
	private static Random random = new Random();

	@Inject(at = @At("TAIL"), method = "tick")
	private void tick(CallbackInfo info) {
		ServerWorld world = getServerWorld();
		ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);
		PlayerInventory inventory = player.getInventory();

		if (world.isClient) {
			return;
		}

		for (int i = 0; i < inventory.size(); i++) {
			tickItem(world, player, inventory, i);
		}
	}

	private void tickItem(ServerWorld world, PlayerEntity player, PlayerInventory inventory, int slot) {
		if (!Mod.config.tickInInventory) {
			return;
		}

		var stack = inventory.getStack(slot);
		var item = stack.getItem();

		if (Mod.config.convertVanillaTorches && item == Items.TORCH) {
			convertVanillaTorch(inventory, stack, slot);
			return;
		}

		if (Mod.config.convertVanillaLanterns && item == Items.LANTERN) {
			convertVanillaLantern(inventory, stack, slot);
			return;
		}

		if (item instanceof TorchItem) {
			tickTorchItem(world, player, inventory, stack, slot);
			tickTorchItemForEnvironmentalConditions(world, player, inventory, slot);
			return;
		}

		if (item instanceof LanternItem) {
			tickLanternItem(world, player, inventory, stack, slot);
			return;
		}

		if (item instanceof ShroomlightItem) {
			tickShroomlightItem(world, player, inventory, stack, slot);
			return;
		}

		if (item instanceof GlowstoneItem) {
			tickGlowstoneItem(world, player, inventory, stack, slot);
			return;
		}
	}

	// Item Conversion

	private void convertVanillaTorch(PlayerInventory inventory, ItemStack stack, int slot) {
		// Remove currently held vanilla torch stack and give a stack of
		// same quantity of mod torches.
		Block torchItem = Mod.config.torchesBurnWhenConverted ? Mod.LIT_TORCH : Mod.UNLIT_TORCH;
		inventory.setStack(slot, new ItemStack(torchItem, stack.getCount()));
	}

	private void convertVanillaLantern(PlayerInventory inventory, ItemStack stack, int slot) {
		// Remove currently held vanilla lantern stack and give a stack of
		// same quantity of mod lanterns.
		Block lanternItem = Mod.config.lanternsBurnWhenConverted ? Mod.LIT_LANTERN : Mod.UNLIT_LANTERN;
		inventory.setStack(slot, new ItemStack(lanternItem, stack.getCount()));
	}

	// Time Tick

	private void tickTorchItem(ServerWorld world, PlayerEntity player, PlayerInventory inventory, ItemStack stack,
			int slot) {
		var item = (TorchItem) stack.getItem();
		var state = item.getTorchState();

		if (state == ETorchState.LIT) {
			var fuelUse = getFuelUseForStack(inventory, stack);
			var modifiedStack = TorchItem.modifiedStackWithAddedFuel(stack, world, -fuelUse);

			inventory.setStack(slot, modifiedStack);

			if (TorchItem.getFuel(modifiedStack) <= 0) {
				item.burnOut(world, player, slot);
			}

			return;
		}

		if (state == ETorchState.SMOLDERING) {
			var fuelUse = getFuelUseForStack(inventory, stack);

			if (random.nextInt(Mod.config.torchesSmolderFuelUseTickChance) == 0) {
				ItemStack modifiedStack = TorchItem.modifiedStackWithAddedFuel(stack, world, -fuelUse);
				inventory.setStack(slot, modifiedStack);
			} else if (random.nextInt(Mod.config.torchesSmolderExtinguishTickChance) == 0) {
				item.extinguish(world, player, slot);
			}

			return;
		}
	}

	private void tickLanternItem(ServerWorld world, PlayerEntity player, PlayerInventory inventory, ItemStack stack,
			int slot) {
		var lanternItem = (LanternItem) stack.getItem();

		if (!lanternItem.isLit) {
			return;
		}

		var fuelUse = getFuelUseForStack(inventory, stack);
		var modifiedStack = LanternItem.addFuel(stack, world, -fuelUse);

		inventory.setStack(slot, modifiedStack);

		if (LanternItem.getFuel(modifiedStack) <= 0) {
			lanternItem.extinguish(world, player, slot);
		}
	}

	private void tickShroomlightItem(ServerWorld world, PlayerEntity player, PlayerInventory inventory, ItemStack stack,
			int slot) {
		if (WorldUtils.worldIsNether(world)) {
			ItemStack modifiedStack = ShroomlightItem.modifiedStackWithAddedFuel(stack, world, 15);
			inventory.setStack(slot, modifiedStack);

			return;
		}

		if (WorldUtils.worldIsDaytime(world)) {
			var fuelUse = getFuelUseForStack(inventory, stack);

			ItemStack modifiedStack = ShroomlightItem.modifiedStackWithAddedFuel(stack, world, -fuelUse);
			inventory.setStack(slot, modifiedStack);

			return;
		}
	}

	private void tickGlowstoneItem(ServerWorld world, PlayerEntity player, PlayerInventory inventory, ItemStack stack,
			int slot) {
		if (WorldUtils.worldIsNether(world)) {
			ItemStack modifiedStack = GlowstoneItem.modifiedStackWithAddedFuel(stack, world, 15);
			inventory.setStack(slot, modifiedStack);

			return;
		}

		var fuelUse = getFuelUseForStack(inventory, stack);

		ItemStack modifiedStack = GlowstoneItem.modifiedStackWithAddedFuel(stack, world, -fuelUse);
		inventory.setStack(slot, modifiedStack);
	}

	// Environmental Conditions Tick

	private void tickTorchItemForEnvironmentalConditions(ServerWorld world, PlayerEntity player,
			PlayerInventory inventory, int slot) {
		var position = player.getBlockPos();
		var stack = inventory.getStack(slot);
		var item = (TorchItem) stack.getItem();

		if (item.getTorchState() == ETorchState.UNLIT) {
			return;
		}

		var isRaining = worldIsRaining(world, position);
		var isUnderwater = player.isSubmergedInWater();

		var rainExtinguishStrategy = Mod.config.invExtinguishInRain;
		var underwaterExtinguishStrategy = Mod.config.invExtinguishInWater;

		// Extinguishing Strategy:
		// `invExtinguishInWater=0`, never extinguish in water
		// `invExtinguishInWater=1`, extinguish if held in hand
		// `invExtinguishInWater=2`, extinguish any

		boolean isInHand = stackIsInHand(inventory, stack);
		boolean shouldExtinguishTorchInRain = isRaining && rainExtinguishStrategy != 0
				&& (rainExtinguishStrategy == 2 || isInHand);
		boolean shouldExtinguishTorchInWater = isUnderwater && underwaterExtinguishStrategy != 0
				&& (underwaterExtinguishStrategy == 2 || isInHand);

		// Rain
		if (shouldExtinguishTorchInRain) {
			extinguishTorchItemInRain(world, player, stack, slot);
		}

		// Underwater
		if (shouldExtinguishTorchInWater) {
			extinguishTorchItemInWater(world, player, stack, slot, isInHand);
		}
	}

	private void extinguishTorchItemInWater(ServerWorld world, PlayerEntity player, ItemStack stack, int slot,
			boolean isInHand) {
		var torchItem = (TorchItem) stack.getItem();
		torchItem.extinguish(world, player, slot);
	}

	private void extinguishTorchItemInRain(ServerWorld world, PlayerEntity player, ItemStack stack, int slot) {
		var torchItem = (TorchItem) stack.getItem();
		var torchState = torchItem.getTorchState();

		if (torchState == ETorchState.UNLIT) {
			return;
		}

		torchItem.extinguish(world, player, slot);
	}

	// Utility

	private boolean worldIsRaining(ServerWorld world, BlockPos position) {
		return world.hasRain(position);
	}

	private boolean stackIsInHand(PlayerInventory inventory, ItemStack stack) {
		return stack == inventory.getMainHandStack() || stack == inventory.offHand.get(0);
	}

	private int getFuelUseForStack(PlayerInventory inventory, ItemStack stack) {
		int fuelUse = 1;

		if (stackIsInHand(inventory, stack)) {
			fuelUse = fuelUse * Mod.config.itemFuelUseMultiplierWhenHeld;

			// For: Mod.config.itemFuelUseJitterChanceWhenHeld
			// Take half of jitter chance as probability for jitter to be applied per tick.
			// The higher the jitter value, the higher the chance, zero means no chance.

			if (Mod.config.itemFuelUseJitterChanceWhenHeld > 0) {
				float jitterRawValue = (float) Mod.config.itemFuelUseJitterChanceWhenHeld;
				int jitterRandomRange = (int) Math.max(0, (1 / (jitterRawValue + 1)) * 1000 - 1);

				if (jitterRandomRange == 0 || random.nextInt(jitterRandomRange) == 0) {
					int jitterFuelMax = (int) Math.max(1, jitterRawValue / 50);
					int jitterFuelUse = random.nextInt(0, jitterFuelMax + 1);

					fuelUse = fuelUse + jitterFuelUse;

					Mod.LOGGER
							.info("Fuel use in tick with jitter applied: " + fuelUse + " with jitter raw value: "
									+ jitterRawValue + " and jitter random range: " + jitterRandomRange
									+ ", jitter fuel use: " + jitterFuelUse);
				} else {

					Mod.LOGGER
							.info("Fuel use in tick without jitter applied: " + fuelUse + " with jitter raw value: "
									+ jitterRawValue + " and jitter random range of: " + jitterRandomRange);
				}
			} else {
				Mod.LOGGER.info("Fuel use in tick: " + fuelUse);
			}
		}

		return fuelUse;
	}

}