package net.qxeii.hardcore_torches.block;

import java.util.function.IntSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.blockentity.FuelBlockEntity;
import net.qxeii.hardcore_torches.blockentity.GlowstoneBlockEntity;
import net.qxeii.hardcore_torches.blockentity.LightableBlock;

public abstract class AbstractGlowstoneBlock extends BlockWithEntity
		implements LightableBlock {
	public IntSupplier maxFuel;

	protected AbstractGlowstoneBlock(Settings settings, IntSupplier maxFuel) {
		super(settings);
		this.maxFuel = maxFuel;
	}

	// region BlockEntity code
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new GlowstoneBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return checkType(type, Mod.GLOWSTONE_BLOCK_ENTITY,
				(world1, pos, state1, be) -> GlowstoneBlockEntity.tick(world1, pos, state1, be));
	}

	// endregion
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
			ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		BlockEntity be = world.getBlockEntity(pos);
		((FuelBlockEntity) be).setFuel(Mod.config.defaultGlowstoneFuel);
	}
}
