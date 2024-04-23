package net.qxeii.hardcore_torches.item;

import static net.minecraft.util.math.MathHelper.clamp;

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
import net.minecraft.util.math.random.Random;
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

		this.torchGroup = group;
		this.torchState = torchState;
		this.maxFuel = maxFuel;
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

		if (nbt == null) {
			return Mod.config.defaultTorchFuel;
		}

		return clamp(nbt.getInt("Fuel"), 0, Mod.config.defaultTorchFuel);
	}

	public static boolean isUsed(ItemStack stack) {
		int fuel = getFuel(stack);

		if (fuel < Mod.config.defaultTorchFuel) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return isUsed(stack);
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
		var state = getTorchState();

		stack = modifiedStackWithAddedFuel(stack, world, -Mod.config.torchesExtinguishFuelLoss);

		if (getFuel(stack) == 0) {
			// Torch is expended, break and remove.
			stack = modifiedStackWithState(stack, ETorchState.BURNT);
			inventory.setStack(slot, stack);
		} else {
			// Torch still has fuel left, can smolder or become unlit.
			if (state == ETorchState.LIT && Mod.config.torchesSmolder) {
				stack = modifiedStackWithState(stack, ETorchState.SMOLDERING);
			} else {
				stack = modifiedStackWithState(stack, ETorchState.UNLIT);
			}

			inventory.setStack(slot, stack);
		}

		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.PLAYERS, 1f,
				1f);
	}

	public void burnOut(World world, PlayerEntity player, int slot) {
		PlayerInventory inventory = player.getInventory();
		ItemStack stack = inventory.getStack(slot);

		stack = modifiedStackWithState(stack, ETorchState.BURNT);
		inventory.setStack(slot, stack);

		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.PLAYERS, 1f, 1f);
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
				player.dropItem(deductedTorchStack, true);
			} else {
				inventory.setStack(emptySlot, deductedTorchStack);
			}

			ItemStack heldTorchStack = modifiedStackWithState(stack, ETorchState.LIT);
			heldTorchStack.setCount(1);

			inventory.setStack(slot, heldTorchStack);
		} else {
			ItemStack heldTorchStack = modifiedStackWithState(stack, ETorchState.LIT);
			inventory.setStack(slot, heldTorchStack);
		}

		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.5f, 1.0f);
	}

	// Interaction

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (world.isClient) {
			return super.use(world, player, hand);
		}

		ItemStack stack = player.getStackInHand(hand);
		ETorchState torchState = ((TorchItem) stack.getItem()).getTorchState();

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
	public boolean onClicked(ItemStack lhsStack, ItemStack rhsStack, Slot slot, ClickType clickType,
			PlayerEntity player,
			StackReference cursorStackReference) {
		if (!slot.canTakePartial(player) || rhsStack.isEmpty() || !(rhsStack.getItem() instanceof TorchItem)) {
			return super.onClicked(lhsStack, rhsStack, slot, clickType, player, cursorStackReference);
		}

		if (isUsed(lhsStack) || isUsed(rhsStack)) {
			return false;
		}

		var lhsTorchItem = (TorchItem) lhsStack.getItem();
		var rhsTorchItem = (TorchItem) rhsStack.getItem();

		if (!equalTorchGroup(lhsTorchItem, rhsTorchItem)) {
			return false;
		}

		return super.onClicked(lhsStack, rhsStack, slot, clickType, player, cursorStackReference);
	}

	public boolean equalTorchGroup(TorchItem lhsItem, TorchItem rhsItem) {
		if (lhsItem.torchGroup == rhsItem.torchGroup) {
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

			if (newState == ETorchState.BURNT) {
				outputStack.setNbt(null);
			}
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
		if (world.isClient) {
			return stack;
		}

		if (!(stack.getItem() instanceof TorchItem)) {
			return stack;
		}

		var fuel = getFuel(stack);
		var nbt = stack.getNbt();

		if (nbt == null) {
			nbt = new NbtCompound();
		}

		fuel = clamp(fuel + amount, 0, Mod.config.defaultTorchFuel);

		if (fuel == 0) {
			if (Mod.config.burntStick) {
				stack = new ItemStack(Items.STICK, stack.getCount());
			} else {
				stack = modifiedStackWithState(stack, ETorchState.BURNT);
			}

			return stack;
		}

		nbt.putInt("Fuel", fuel);

		if (fuel < Mod.config.defaultTorchFuel && fuel > 0) {
			nbt.putInt("Salt", Random.create().nextInt());
		} else {
			nbt.remove("Salt");
		}

		stack.setNbt(nbt);
		return stack;
	}

}
