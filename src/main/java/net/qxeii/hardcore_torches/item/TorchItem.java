package net.qxeii.hardcore_torches.item;

import net.qxeii.hardcore_torches.util.ETorchState;
import net.qxeii.hardcore_torches.util.TorchGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.block.AbstractHardcoreTorchBlock;

public class TorchItem extends VerticallyAttachableBlockItem {
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

		if (oldNbt == null && newNbt != null)
			return true;
		if (oldNbt != null && newNbt == null)
			return true;
		if (oldNbt == null && newNbt == null)
			return false;

		return oldNbt.equals(null);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		// If torch is unlit and player has `minecraft:flint_and_steel` in inventory,
		// light torch. Use one condition from flint and steel.

		if (world.isClient) {
			return super.use(world, player, hand);
		}

		switch (torchState) {
			case UNLIT, SMOLDERING: {
				if (!useFlintAndSteelFromPlayerInventory(player, player.getInventory())) {
					return super.use(world, player, hand);
				}

				lightTorchInHand(world, player, hand);
				return super.use(world, player, hand);
			}
			case LIT: {
				unlightTorchInHand(world, player, hand);
				return super.use(world, player, hand);
			}
			default:
				return super.use(world, player, hand);
		}
	}

	// Torch Lighting in Hand Mechanics

	private void unlightTorchInHand(World world, PlayerEntity player, Hand hand) {
		// Get lit torch in either hand.

		HandStackTuple handStackTuple = getTorchStackTupleInHandFromPlayer(player, hand);

		if (handStackTuple == null || torchState != ETorchState.LIT) {
			return;
		}

		ItemStack heldTorchStack = handStackTuple.stack;

		if (Mod.config.torchesSmolder) {
			heldTorchStack = stateStack(heldTorchStack, ETorchState.SMOLDERING);
		} else {
			heldTorchStack = stateStack(heldTorchStack, ETorchState.UNLIT);
		}

		heldTorchStack = addFuel(heldTorchStack, world, -Mod.config.torchesExtinguishConditionLoss);

		PlayerInventory inventory = player.getInventory();
		inventory.setStack(handStackTuple.slot, heldTorchStack);
		player.setStackInHand(handStackTuple.hand, heldTorchStack);

		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f,
				1.0f);
	}

	private void lightTorchInHand(World world, PlayerEntity player, Hand hand) {
		// Get unlit torch in either hand.
		// (Branch A) If held stack contains a stack of items, remove one, move
		// remaining stack to inventory,
		// and replace held stack with new stack of one lit torch.
		// (Branch B) If held stack contains only a single item, read torch damage
		// value, and replace held stack
		// with new stack of one lit torch with same torch damage value.

		PlayerInventory inventory = player.getInventory();
		HandStackTuple handStackTuple = getTorchStackTupleInHandFromPlayer(player, hand);

		if (handStackTuple == null || torchState == ETorchState.LIT || torchState == ETorchState.BURNT) {
			return;
		}

		if (handStackTuple.stack.getCount() > 1) {
			int emptySlot = inventory.getEmptySlot();
			ItemStack deductedTorchStack = handStackTuple.stack.copyWithCount(handStackTuple.stack.getCount() - 1);

			if (emptySlot == -1) {
				player.dropItem(deductedTorchStack, true);
			} else {
				inventory.setStack(emptySlot, deductedTorchStack);
			}

			ItemStack heldTorchStack = stateStack(handStackTuple.stack, ETorchState.LIT);
			heldTorchStack.setCount(1);

			inventory.setStack(handStackTuple.slot, heldTorchStack);
		} else {
			ItemStack heldTorchStack = stateStack(handStackTuple.stack, ETorchState.LIT);
			inventory.setStack(handStackTuple.slot, heldTorchStack);
		}

		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.5f, 1.0f);
	}

	// Torch Inventory Utilities

	private HandStackTuple getTorchStackTupleInHandFromPlayer(PlayerEntity player, Hand hand) {
		PlayerInventory inventory = player.getInventory();

		switch (hand) {
			case MAIN_HAND: {
				int selectedSlot = inventory.selectedSlot;
				ItemStack mainHandStack = inventory.getStack(selectedSlot);
				return new HandStackTuple(Hand.MAIN_HAND, selectedSlot, mainHandStack);
			}
			case OFF_HAND: {
				int selectedSlot = PlayerInventory.OFF_HAND_SLOT;
				ItemStack offHandStack = inventory.getStack(selectedSlot);
				return new HandStackTuple(Hand.OFF_HAND, selectedSlot, offHandStack);
			}
			default:
				return null;
		}
	}

	private static class HandStackTuple {
		public Hand hand;
		public int slot;
		public ItemStack stack;

		public HandStackTuple(Hand hand, int slot, ItemStack stack) {
			this.hand = hand;
			this.slot = slot;
			this.stack = stack;
		}
	}

	// Flint and Steel Inventory Utilities

	private static boolean useFlintAndSteelFromPlayerInventory(PlayerEntity player, PlayerInventory inventory) {
		// Get flint and steel from player inventory, resolve as stack.

		int flintAndSteelItemSlot = getFlintAndSteelSlotFromPlayerInventory(inventory);

		if (flintAndSteelItemSlot == -1) {
			return false;
		}

		ItemStack flintAndSteelItemStack = inventory.getStack(flintAndSteelItemSlot);

		flintAndSteelItemStack.damage(1, player, (playerEntity) -> {
			ItemStack actualHandStack = player.getMainHandStack();
			player.equipStack(EquipmentSlot.MAINHAND, flintAndSteelItemStack);
			player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
			player.equipStack(EquipmentSlot.MAINHAND, actualHandStack);

			if (FabricLoader.getInstance().isModLoaded("ruined_equipment")) {
				// Get ruined flint and steel from the ruined_equipment mod.

				Identifier ruinedFlintAndSteelId = new Identifier("ruined_equipment", "ruined_flint_and_steel");
				Item ruinedFlintAndSteel = Registries.ITEM.get(ruinedFlintAndSteelId);

				// Check if the item is registered.
				if (ruinedFlintAndSteel != Items.AIR) {
					// Create an ItemStack of the ruined flint and steel.
					ItemStack ruinedFlintAndSteelStack = new ItemStack(ruinedFlintAndSteel, 1);

					// Add ruined flint and steel to player inventory.
					player.getInventory().setStack(flintAndSteelItemSlot, ruinedFlintAndSteelStack);
				}
			}
		});

		return true;
	}

	private static int getFlintAndSteelSlotFromPlayerInventory(PlayerInventory inventory) {
		// Iterates through all stacks in the inventory and returns the flint and steel
		// with the lowest condition.
		int highestDamage = 0;
		int highestDamageItemSlot = -1;

		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			Item item = stack.getItem();

			if (item != Items.FLINT_AND_STEEL) {
				continue;
			}

			int itemDamage = stack.getDamage();

			if (highestDamageItemSlot == -1 || itemDamage > highestDamage) {
				highestDamage = itemDamage;
				highestDamageItemSlot = i;
			}
		}

		return highestDamageItemSlot;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		ItemStack stack = context.getStack();
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);

		// Make sure it's a torch and get its type
		if (stack.getItem() instanceof TorchItem) {
			ETorchState torchState = ((TorchItem) stack.getItem()).torchState;

			if (torchState == ETorchState.UNLIT || torchState == ETorchState.SMOLDERING) {

				// Unlit and Smoldering
				if (state.isIn(Mod.FREE_TORCH_LIGHT_BLOCKS)) {
					// No lighting on unlit fires etc.
					if (state.contains(Properties.LIT))
						if (state.get(Properties.LIT).booleanValue() == false)
							return super.useOnBlock(context);

					PlayerEntity player = context.getPlayer();
					if (player != null && !world.isClient)
						player.setStackInHand(context.getHand(), stateStack(stack, ETorchState.LIT));
					if (!world.isClient)
						world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 1.2f);
					return ActionResult.SUCCESS;
				}
			}
		}

		return super.useOnBlock(context);
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player,
			StackReference cursorStackReference) {
		// If you are clicking on it with a non HCTorch item or with empty, use vanilla
		// behavior
		if (!slot.canTakePartial(player) || !(otherStack.getItem() instanceof TorchItem) || otherStack.isEmpty()) {
			return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
		}

		// Return left click if either is full
		if (clickType != ClickType.RIGHT
				&& (stack.getCount() >= stack.getMaxCount() || otherStack.getCount() >= otherStack.getMaxCount())) {
			return false;
		}

		// Ensure torches are in same group
		if (!sameTorchGroup((TorchItem) stack.getItem(), (TorchItem) otherStack.getItem())) {
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
			int otherMax = otherStack.getMaxCount();

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

	public boolean sameTorchGroup(TorchItem item1, TorchItem item2) {
		if (item1.torchGroup == item2.torchGroup) {
			return true;
		}
		return false;
	}

	public static Item stateItem(Item inputItem, ETorchState newState) {
		Item outputItem = Items.AIR;

		if (inputItem instanceof BlockItem && inputItem instanceof TorchItem) {
			AbstractHardcoreTorchBlock newBlock = (AbstractHardcoreTorchBlock) ((BlockItem) inputItem).getBlock();
			TorchItem newItem = (TorchItem) newBlock.group.getStandingTorch(newState).asItem();

			outputItem = newItem;
		}

		return outputItem;
	}

	public static ItemStack stateStack(ItemStack inputStack, ETorchState newState) {
		ItemStack outputStack = ItemStack.EMPTY;

		if (inputStack.getItem() instanceof BlockItem && inputStack.getItem() instanceof TorchItem) {
			AbstractHardcoreTorchBlock newBlock = (AbstractHardcoreTorchBlock) ((BlockItem) inputStack.getItem())
					.getBlock();
			TorchItem newItem = (TorchItem) newBlock.group.getStandingTorch(newState).asItem();

			outputStack = modifiedItemForReplacement(inputStack, newItem);
			if (newState == ETorchState.BURNT)
				outputStack.setNbt(null);
		}

		return outputStack;
	}

	public ETorchState getTorchState() {
		return torchState;
	}

	public TorchGroup getTorchGroup() {
		return torchGroup;
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

	public static int getFuel(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		int fuel;

		if (nbt != null) {
			return nbt.getInt("Fuel");
		}

		return Mod.config.defaultTorchFuel;
	}

	public static ItemStack addFuel(ItemStack stack, World world, int amount) {
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
					stack = stateStack(stack, ETorchState.BURNT);
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

	// Private Utilties
}
