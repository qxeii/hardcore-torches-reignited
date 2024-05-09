package net.qxeii.hardcore_torches.block;

import static net.minecraft.state.property.Properties.CANDLES;

import java.util.function.IntSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.blockentity.CandleBlockEntity;
import net.qxeii.hardcore_torches.blockentity.FuelBlockEntity;
import net.qxeii.hardcore_torches.blockentity.LightableBlock;

public abstract class AbstractCandleBlock extends BlockWithEntity implements LightableBlock {
	public static final BooleanProperty LIT;
	public IntSupplier maxFuel;
	public boolean isLit;

	protected AbstractCandleBlock(AbstractBlock.Settings settings, IntSupplier maxFuel, boolean isLit) {
		super(settings);
		this.maxFuel = maxFuel;
		this.isLit = isLit;
	}

	protected abstract Iterable<Vec3d> getParticleOffsets(BlockState state);

	public static boolean isLitCandle(BlockState state) {
		return (Boolean) state.get(LIT) && (state.isIn(BlockTags.CANDLES) || state.isIn(BlockTags.CANDLE_CAKES))
				&& (Boolean) state.get(LIT);
	}

	public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
		if (world.isClient) {
			return;
		}

		if (projectile.isOnFire() && this.isNotLit(state)) {
			setLit(world, state, hit.getBlockPos(), true);
		}
	}

	protected boolean isNotLit(BlockState state) {
		return !(Boolean) state.get(LIT);
	}

	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if ((Boolean) state.get(LIT)) {
			this.getParticleOffsets(state).forEach((offset) -> {
				spawnCandleParticles(world, offset.add((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()),
						random);
			});
		}
	}

	private static void spawnCandleParticles(World world, Vec3d vec3d, Random random) {
		float f = random.nextFloat();
		if (f < 0.3F) {
			world.addParticle(ParticleTypes.SMOKE, vec3d.x, vec3d.y, vec3d.z, 0.0D, 0.0D, 0.0D);
			if (f < 0.17F) {
				world.playSound(vec3d.x + 0.5D, vec3d.y + 0.5D, vec3d.z + 0.5D, SoundEvents.BLOCK_CANDLE_AMBIENT,
						SoundCategory.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
			}
		}

		world.addParticle(ParticleTypes.SMALL_FLAME, vec3d.x, vec3d.y, vec3d.z, 0.0D, 0.0D, 0.0D);
	}

	public static void extinguish(@Nullable PlayerEntity player, BlockState state, WorldAccess world, BlockPos pos) {
		setLit(world, state, pos, false);
		if (state.getBlock() instanceof AbstractCandleBlock) {
			((AbstractCandleBlock) state.getBlock()).getParticleOffsets(state).forEach((offset) -> {
				world.addParticle(ParticleTypes.SMOKE, (double) pos.getX() + offset.getX(),
						(double) pos.getY() + offset.getY(), (double) pos.getZ() + offset.getZ(), 0.0D,
						0.10000000149011612D, 0.0D);
			});
		}
		world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F,
				1.0F);
		world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	public static void melt(BlockState state, World world, BlockPos pos) {
		BlockEntity be = world.getBlockEntity(pos);
		((FuelBlockEntity) be).setFuel(Mod.config.defaultCandleFuel);
		be.markDirty();
		changeCandles(world, state, pos, -1);
		((AbstractCandleBlock) state.getBlock()).getParticleOffsets(state).forEach((offset) -> {
			world.addParticle(ParticleTypes.SMOKE, (double) pos.getX() + offset.getX(),
					(double) pos.getY() + offset.getY(), (double) pos.getZ() + offset.getZ(), 0.0D, 0.001D, 0.0D);
		});

		world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F,
				1.0F);
		world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
	}

	public static void ignite(@Nullable PlayerEntity player, BlockState state, World world, BlockPos pos) {
		setLit(world, state, pos, true);
		if (state.getBlock() instanceof CandleBlock) {
			((AbstractCandleBlock) state.getBlock()).getParticleOffsets(state).forEach((offset) -> {
				world.addParticle(ParticleTypes.SMALL_FLAME, (double) pos.getX() + offset.getX(),
						(double) pos.getY() + offset.getY(), (double) pos.getZ() + offset.getZ(), 0.0D, 0.0D, 0.0D);
			});
		}
		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 2, 1);
		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 2, 2);
		world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1f, 1f);
		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 2f, 2f);
		world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	public static void setLit(WorldAccess world, BlockState state, BlockPos pos, boolean lit) {
		BlockState newState = state.with(LIT, lit);
		world.setBlockState(pos, newState, 11);
	}

	public static void changeCandles(WorldAccess world, BlockState state, BlockPos pos, int candles) {
		int newCandles = state.get(CANDLES) + candles;
		setCandles(world, state, pos, newCandles);
	}

	public static void setCandles(WorldAccess world, BlockState state, BlockPos pos, int candles) {
		if (candles <= 0) {
			world.removeBlock(pos, false);
		} else {
			BlockState newState = state.with(CANDLES, candles);
			world.setBlockState(pos, newState, 3);
		}
	}

	@Override
	public void onOutOfFuel(World world, BlockPos pos, BlockState state, boolean playSound) {
		((AbstractCandleBlock) world.getBlockState(pos).getBlock()).melt(state, world, pos);
	}

	// region BlockEntity code
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new CandleBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return checkType(type, Mod.CANDLE_BLOCK_ENTITY,
				(world1, pos, state1, be) -> CandleBlockEntity.tick(world1, pos, state1, be));
	}

	// endregion
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
			ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		BlockEntity be = world.getBlockEntity(pos);
		((FuelBlockEntity) be).setFuel(Mod.config.defaultCandleFuel);
	}

	static {
		LIT = Properties.LIT;
	}
}
