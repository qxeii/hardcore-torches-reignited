package net.qxeii.hardcore_torches.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.block.AbstractTorchBlock;
import net.qxeii.hardcore_torches.util.ETorchState;
import net.qxeii.hardcore_torches.util.TorchGroup;

public class TorchItem extends VerticallyAttachableBlockItem implements LightableItem {

	ETorchState torchState;
	TorchGroup torchGroup;
	int maxFuel;

	public TorchItem(Block standingBlock, Block wallBlock, Item.Settings settings, ETorchState torchState, int maxFuel,
			TorchGroup group) {
		super(standingBlock, wallBlock, settings, Direction.DOWN);
		this.torchState = torchState;
		this.maxFuel = maxFuel;
		this.torchGroup = group;
	}

	// State & Properties

	public ETorchState getTorchState() {
		return torchState;
	}

	public TorchGroup getTorchGroup() {
		return torchGroup;
	}

	public static int getFuel(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();

		if (nbt != null) {
			return nbt.getInt("Fuel");
		}

		return Mod.config.defaultTorchFuel;
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		int fuel = getFuel(stack);

		if (fuel > 0 && fuel < maxFuel) {
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

		if (oldNbt == null && newNbt != null) {
			return true;
		}

		if (oldNbt != null && newNbt == null) {
			return true;
		}

		if (oldNbt == null && newNbt == null) {
			return false;
		}

		return oldNbt == null || oldNbt.equals(null);
	}

	// Actions

	public void extinguish(World world, PlayerEntity player, int slot) {
		PlayerInventory inventory = player.getInventory();
		ItemStack stack = inventory.getStack(slot);

		stack = modifiedStackWithAddedFuel(stack, world, -Mod.config.torchesExtinguishFuelLoss);

		if (getFuel(stack) <= 0) {
			// Torch is expended, break and remove.
			stack = modifiedStackWithState(stack, ETorchState.BURNT);
			player.getInventory().setStack(slot, stack);
		} else {
			if (Mod.config.torchesSmolder) {
				stack = modifiedStackWithState(stack, ETorchState.SMOLDERING);
			} else {
				stack = modifiedStackWithState(stack, ETorchState.UNLIT);
			}

			inventory.setStack(slot, stack);
		}

		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.PLAYERS, 1f,
				1f);
	}

	public void light(World world, PlayerEntity player, int slot) {
		var inventory = player.getInventory();
		var stack = inventory.getStack(slot);

		if (stack.getCount() > 1) {
			// Holding multiple unlit (unused) torches in stack.
			// Stack must be split, remaining stack of torches moved to free inventory slot,
			// and one new stack created with lit torch in hand slot.

			int emptySlot = inventory.getEmptySlot();
			ItemStack deductedTorchStack = stack.copyWithCount(stack.getCount() - 1);

			if (emptySlot == -1) {
				Mod.LOGGER.debug("Lighting torch in stack of unlit torches, no empty slot found, dropping stack.");
				player.dropItem(deductedTorchStack, true);
			} else {
				Mod.LOGGER.debug("Lighting torch in stack of unlit torches, moving remaining torches to empty slot.");
				inventory.setStack(emptySlot, deductedTorchStack);
			}

			Mod.LOGGER.debug("Lighting torch in stack of unlit torches, creating new stack with lit torch in hand.");
			ItemStack heldTorchStack = modifiedStackWithState(stack, ETorchState.LIT);
			heldTorchStack.setCount(1);

			inventory.setStack(slot, heldTorchStack);
		} else {
			Mod.LOGGER.debug("Lighting single unlit torch in hand.");

			ItemStack heldTorchStack = modifiedStackWithState(stack, ETorchState.LIT);
			inventory.setStack(slot, heldTorchStack);
		}

		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.5f, 1.0f);
	}

	// Interaction

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		// If torch is unlit and player has `minecraft:flint_and_steel` in inventory,
		// light torch. Use one condition from flint and steel.

		if (world.isClient) {
			return super.use(world, player, hand);
		}

		ItemStack stack = player.getStackInHand(hand);
		ETorchState torchState = ((TorchItem) stack.getItem()).getTorchState();

		Mod.LOGGER.debug("Using torch with state: {}", torchState.toString());

		switch (torchState) {
			case UNLIT, SMOLDERING: {
				useLighterAndLightWithInteraction(world, player, hand);
				return super.use(world, player, hand);
			}
			case LIT: {
				extinguishWithInteraction(world, player, hand);
				return super.use(world, player, hand);
			}
			default: {
				return super.use(world, player, hand);
			}
		}
	}

	public void extinguishWithInteraction(World world, PlayerEntity player, Hand hand) {
		PlayerInventory inventory = player.getInventory();
		int slot = hand == Hand.MAIN_HAND ? inventory.selectedSlot : PlayerInventory.OFF_HAND_SLOT;

		extinguish(world, player, slot);
		player.swingHand(hand);
	}

	public boolean useLighterAndLightWithInteraction(World world, PlayerEntity player, Hand hand) {
		if (!findAndUseLighterItem(player, hand)) {
			return false;
		}

		return lightWithInteraction(world, player, hand);
	}

	public boolean lightWithInteraction(World world, PlayerEntity player, Hand hand) {
		var inventory = player.getInventory();
		var slot = hand == Hand.MAIN_HAND ? inventory.selectedSlot : PlayerInventory.OFF_HAND_SLOT;
		var stack = inventory.getStack(slot);
		var state = ((TorchItem) stack.getItem()).getTorchState();

		if (state == ETorchState.LIT || state == ETorchState.BURNT) {
			Mod.LOGGER.debug("Torch is already lit or burnt, can not light.");
			return false;
		}

		light(world, player, slot);
		player.swingHand(hand);

		return true;
	}

	// Events

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		// Light torch in hand on existing torch.

		// var world = context.getWorld();
		// var blockPosition = context.getBlockPos();
		// var blockEntity = world.getBlockEntity(blockPosition);
		// var blockState = world.getBlockState(blockPosition);
		// var stack = context.getStack();

		return super.useOnBlock(context);
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player,
			StackReference cursorStackReference) {
		// If you are clicking on it with a non HCTorch item or with empty,
		// use vanilla behavior.
		if (!slot.canTakePartial(player) || !(otherStack.getItem() instanceof TorchItem) || otherStack.isEmpty()) {
			return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
		}

		// Return left click if either is full
		if (clickType != ClickType.RIGHT
				&& (stack.getCount() >= stack.getMaxCount() || otherStack.getCount() >= otherStack.getMaxCount())) {
			return false;
		}

		// Ensure torches are in same group
		if (!equalTorchGroup((TorchItem) stack.getItem(), (TorchItem) otherStack.getItem())) {
			return false;
		}

		if (((TorchItem) stack.getItem()).torchState == ETorchState.LIT) {
			// If clicked is lit, return if clicked with burnt
			if (((TorchItem) otherStack.getItem()).torchState == ETorchState.BURNT) {
				return false;
			}
		} else if (((TorchItem) stack.getItem()).torchState == ETorchState.UNLIT) {
			// If clicked is unlit, return if clicked is not unlit
			if (((TorchItem) otherStack.getItem()).torchState != ETorchState.UNLIT) {
				return false;
			}
		}

		if (!otherStack.isEmpty()) {
			int max = stack.getMaxCount();
			int usedCount = clickType != ClickType.RIGHT ? otherStack.getCount() : 1;

			int remainder = Math.max(0, usedCount - (max - stack.getCount()));
			int addedNew = usedCount - remainder;

			// Average both stacks
			int stack1Fuel = getFuel(stack) * stack.getCount();
			int stack2Fuel = getFuel(otherStack) * addedNew;
			int totalFuel = stack1Fuel + stack2Fuel;

			// NBT
			NbtCompound nbt = new NbtCompound();
			nbt.putInt("Fuel", totalFuel / (stack.getCount() + addedNew));

			if (addedNew > 0) {
				stack.increment(addedNew);
				stack.setNbt(nbt);
				otherStack.setCount(otherStack.getCount() - addedNew);
				return true;
			}
		}
		return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
	}

	public boolean equalTorchGroup(TorchItem item1, TorchItem item2) {
		if (item1.torchGroup == item2.torchGroup) {
			return true;
		}
		return false;
	}

	// Modification

	public static Item modifiedItemWithState(Item inputItem, ETorchState newState) {
		Item outputItem = Items.AIR;

		if (inputItem instanceof BlockItem && inputItem instanceof TorchItem) {
			AbstractTorchBlock newBlock = (AbstractTorchBlock) ((BlockItem) inputItem).getBlock();
			TorchItem newItem = (TorchItem) newBlock.group.getStandingTorch(newState).asItem();

			outputItem = newItem;
		}

		return outputItem;
	}

	public static ItemStack modifiedStackWithState(ItemStack inputStack, ETorchState newState) {
		ItemStack outputStack = ItemStack.EMPTY;

		if (inputStack.getItem() instanceof BlockItem && inputStack.getItem() instanceof TorchItem) {
			AbstractTorchBlock newBlock = (AbstractTorchBlock) ((BlockItem) inputStack.getItem())
					.getBlock();
			TorchItem newItem = (TorchItem) newBlock.group.getStandingTorch(newState).asItem();

			outputStack = modifiedItemForReplacement(inputStack, newItem);
			if (newState == ETorchState.BURNT)
				outputStack.setNbt(null);
		}

		return outputStack;
	}

	public static ItemStack modifiedItemForReplacement(ItemStack stack, Item replacementItem) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack itemStack = new ItemStack(replacementItem, stack.getCount());

		if (stack.getNbt() != null) {
			itemStack.setNbt(stack.getNbt().copy());
		}

		return itemStack;
	}

	public static ItemStack modifiedStackWithAddedFuel(ItemStack stack, World world, int amount) {
		if (stack.getItem() instanceof TorchItem && !world.isClient) {
			NbtCompound nbt = stack.getNbt();
			int fuel = Mod.config.defaultTorchFuel;

			if (nbt != null) {
				fuel = nbt.getInt("Fuel");
			} else {
				nbt = new NbtCompound();
			}

			fuel += amount;

			// If burn out
			if (fuel <= 0) {
				if (Mod.config.burntStick) {
					stack = new ItemStack(Items.STICK, stack.getCount());
				} else {
					stack = modifiedStackWithState(stack, ETorchState.BURNT);
				}
			} else {
				if (fuel > Mod.config.defaultTorchFuel) {
					fuel = Mod.config.defaultTorchFuel;
				}

				nbt.putInt("Fuel", fuel);
				stack.setNbt(nbt);
			}
		}

		return stack;
	}

}
