package net.qxeii.hardcore_torches.mixinlogic;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.util.WorldUtils;

public interface CampfireBlockMixinLogic {

	// Properties

	public static final BooleanProperty LIT = Properties.LIT;

	public static final BooleanProperty SIGNAL_FIRE = CampfireBlock.SIGNAL_FIRE;

	public static final BooleanProperty WATERLOGGED = CampfireBlock.WATERLOGGED;

	public static final DirectionProperty FACING = CampfireBlock.FACING;

	public StateManager<Block, BlockState> getStateManager();

	public void setDefaultState(BlockState defaultState);

	public BlockState getDefaultState();

	// Placement

	public default BlockState injectedGetPlacementState(ItemPlacementContext context, BlockState state) {
		return state.with(LIT, false);
	}

	// Interaction

	public default ActionResult injectedOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand hand,
			BlockHitResult hit) {
		var blockEntity = (CampfireBlockEntity) world.getBlockEntity(pos);
		var campfireBlockEntity = (CampfireBlockEntityMixinLogic) (Object) blockEntity;
		var isLit = state.get(LIT);

		var stack = player.getStackInHand(hand);

		if (stack.isEmpty() && player.isSneaking()) {
			if (world.isClient && Mod.config.fuelMessage) {
				displayFuelMessage(player, campfireBlockEntity.getFuel());
			}

			return ActionResult.PASS;
		}

		var stackIsShovel = stack.isIn(Mod.CAMPFIRE_SHOVELS);
		var stackIsCoal = stack.isIn(Mod.CAMPFIRE_FUELS);
		var stackIsLog = stack.isIn(Mod.CAMPFIRE_LOG_FUELS);

		if (stackIsShovel) {
			if (!isLit) {
				return ActionResult.PASS;
			}

			CampfireBlockEntityMixinLogic.extinguish(world, pos, state);

			if (world.isClient) {
				player.swingHand(hand);
			}

			return ActionResult.SUCCESS;
		}

		if (!stackIsCoal && !stackIsLog) {
			return ActionResult.PASS;
		}

		var item = stack.getItem();
		var itemFuelValue = FuelRegistry.INSTANCE.get(item) * Mod.config.campfireFuelAdditionMultiplier;

		if (!world.isClient) {
			stack.setCount(stack.getCount() - 1);
			campfireBlockEntity.setFuel(campfireBlockEntity.getFuel() + itemFuelValue);

			if (stackIsLog) {
				world.playSound(null, pos, Mod.CAMPFIRE_LOG_PLACE_SOUND, SoundCategory.BLOCKS, 1.0F, 1.0F);
			} else if (stackIsCoal) {
				world.playSound(null, pos, Mod.CAMPFIRE_LOG_PLACE_SOUND, SoundCategory.BLOCKS, 0.75F, 1.75F);
			}

			if (Mod.config.fuelMessage) {
				displayFuelMessage(player, campfireBlockEntity.getFuel());
			}
		} else {
			player.swingHand(hand);
		}

		return ActionResult.CONSUME;
	}

	private void displayFuelMessage(PlayerEntity player, int fuel) {
		var fuelTimeMessage = WorldUtils.formattedFuelText(fuel);
		player.sendMessage(fuelTimeMessage, true);
	}

}
