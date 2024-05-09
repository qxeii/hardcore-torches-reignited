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
import net.qxeii.hardcore_torches.blockentity.LightableBlock;
import net.qxeii.hardcore_torches.blockentity.ShroomlightBlockEntity;
import net.qxeii.hardcore_torches.item.ShroomlightItem;

public abstract class AbstractShroomlightBlock extends BlockWithEntity implements LightableBlock {
	public IntSupplier maxFuel;
	public boolean isLit;

	protected AbstractShroomlightBlock(Settings settings, IntSupplier maxFuel, boolean isLit) {
		super(settings);
		this.maxFuel = maxFuel;
		this.isLit = isLit;
	}

	// region BlockEntity code
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ShroomlightBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return checkType(type, Mod.SHROOMLIGHT_BLOCK_ENTITY,
				(world1, pos, state1, be) -> ShroomlightBlockEntity.tick(world1, pos, state1, be));
	}

	// endregion
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
			ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		BlockEntity be = world.getBlockEntity(pos);
		if (be != null && be instanceof FuelBlockEntity && itemStack.getItem() instanceof ShroomlightItem) {
			int fuel = ShroomlightItem.getFuel(itemStack);

			((FuelBlockEntity) be).setFuel(fuel);
		} else {
			((FuelBlockEntity) be).setFuel(0);
		}
	}
}
