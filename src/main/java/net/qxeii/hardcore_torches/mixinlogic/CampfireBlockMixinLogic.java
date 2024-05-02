package net.qxeii.hardcore_torches.mixinlogic;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public interface CampfireBlockMixinLogic {

	// Properties

	public static final BooleanProperty LIT = Properties.LIT;

	public static final BooleanProperty SIGNAL_FIRE = CampfireBlock.SIGNAL_FIRE;

	public static final BooleanProperty WATERLOGGED = CampfireBlock.WATERLOGGED;

	public static final DirectionProperty FACING = CampfireBlock.FACING;

	public StateManager<Block, BlockState> getStateManager();

	public void setDefaultState(BlockState defaultState);

	public BlockState getDefaultState();

	public default void injectedInit(boolean emitsParticles, int fireDamage, AbstractBlock.Settings settings) {
		// Overwrite default state to be unlit.

		this.setDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.getStateManager()
				.getDefaultState()).with(LIT, false)).with(SIGNAL_FIRE, false)).with(WATERLOGGED, false))
				.with(FACING, Direction.NORTH));
	}

	public default BlockState injectedGetPlacementState(ItemPlacementContext context, BlockState state) {
		// var worldAccess = context.getWorld();
		// var blockPos = context.getBlockPos();
		// var isBlockInWater = worldAccess.getFluidState(blockPos).getFluid() ==
		// Fluids.WATER;
		// var isLit = false; // not isBlockInWater

		return state.with(LIT, false);
	}

	private boolean isSignalFireBaseBlock(BlockState state) {
		return state.isOf(Blocks.HAY_BLOCK);
	}

}
