package net.qxeii.hardcore_torches.block;

import java.util.function.IntSupplier;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.qxeii.hardcore_torches.util.ETorchState;

public class FloorTorchBlock extends AbstractTorchBlock {

	public FloorTorchBlock(Settings settings, ParticleEffect particle, ETorchState type, IntSupplier maxFuel) {
		super(settings, particle, type, maxFuel);
	}

	// region Overridden methods for TorchBlock since I can't extend 2 classes
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return Blocks.TORCH.getOutlineShape(state, world, pos, context);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
			WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		return Blocks.TORCH.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return Blocks.TORCH.canPlaceAt(state, world, pos);
	}
	// endregion

	@Override
	public boolean isWall() {
		return false;
	}
}