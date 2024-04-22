package net.qxeii.hardcore_torches.block;

import java.util.function.IntSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.blockentity.FuelBlockEntity;
import net.qxeii.hardcore_torches.blockentity.LightableBlock;
import net.qxeii.hardcore_torches.blockentity.TorchBlockEntity;
import net.qxeii.hardcore_torches.item.TorchItem;
import net.qxeii.hardcore_torches.util.ETorchState;
import net.qxeii.hardcore_torches.util.TorchGroup;
import net.qxeii.hardcore_torches.util.TorchUtils;

public abstract class AbstractTorchBlock extends BlockWithEntity implements LightableBlock {

	public ParticleEffect particle;
	public ETorchState burnState;
	public TorchGroup group;
	public IntSupplier maxFuel;

	public AbstractTorchBlock(AbstractBlock.Settings settings, ParticleEffect particle, ETorchState type,
			IntSupplier maxFuel) {
		super(settings);
		this.particle = particle;
		this.burnState = type;
		this.maxFuel = maxFuel;
	}

	// Properties

	public abstract boolean isWall();

	public ETorchState getBurnState() {
		return burnState;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		// With inheriting from BlockWithEntity this defaults to INVISIBLE.
		return BlockRenderType.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return checkType(type, Mod.TORCH_BLOCK_ENTITY,
				(world1, pos, state1, be) -> TorchBlockEntity.tick(world1, pos, state1, be));
	}

	// Entities

	@Override
	public BlockEntity createBlockEntity(BlockPos position, BlockState state) {
		return new TorchBlockEntity(position, state);
	}

	// Actions

	public void smother(World world, BlockPos position, BlockState state, boolean playSound) {
		if (world.isClient) {
			return;
		}
		if (playSound) {
			world.playSound(null, position, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
		}

		TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, position);
		TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, position);
		TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, position);
		TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, position);

		modifyTorch(world, position, state, ETorchState.SMOLDERING);
	}

	public void extinguish(World world, BlockPos pos, BlockState state, boolean playSound) {
		if (!world.isClient) {
			return;
		}

		if (playSound) {
			world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
		}

		TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
		TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
		TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, pos);
		TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, pos);

		modifyTorch(world, pos, state, ETorchState.UNLIT);
	}

	public void burnOut(World world, BlockPos pos, BlockState state, boolean playSound) {
		if (!world.isClient) {
			return;
		}

		if (playSound) {
			world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
		}

		TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
		TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
		TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, pos);
		TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, pos);

		modifyTorch(world, pos, state, ETorchState.BURNT);
	}

	public void light(World world, BlockPos pos, BlockState state) {
		if (!world.isClient) {
			return;
		}

		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 2, 1);
		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 2, 2);
		world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.3f, 2f);
		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 2f, 2f);

		TorchUtils.displayParticle(ParticleTypes.LAVA, state, world, pos);
		TorchUtils.displayParticle(ParticleTypes.FLAME, state, world, pos);

		modifyTorch(world, pos, state, ETorchState.LIT);
	}

	@Override
	public void onOutOfFuel(World world, BlockPos position, BlockState state, boolean playSound) {
		burnOut(world, position, state, playSound);
	}

	// Interaction

	public ActionResult onUse(BlockState state, World world, BlockPos position, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		ItemStack stack = player.getStackInHand(hand);

		// Extinguishing

		if (stack.isEmpty() && burnState == ETorchState.LIT) {
			extinguishWithInteraction(world, position, state, player, stack, hand);
			return ActionResult.SUCCESS;
		}

		// Lighting

		if (burnState == ETorchState.SMOLDERING || burnState == ETorchState.UNLIT) {
			if (!useFuelAndLightWithInteraction(world, position, state, player, stack, hand)) {
				return ActionResult.PASS;
			}

			BlockEntity blockEntity = world.getBlockEntity(position);

			if (!world.isClient && blockEntity.getType() == Mod.TORCH_BLOCK_ENTITY && Mod.config.fuelMessage
					&& stack.isEmpty()) {
				player.sendMessage(Text.of("Fuel: " + ((TorchBlockEntity) blockEntity).getFuel()), true);
			}

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	private void extinguishWithInteraction(World world, BlockPos position, BlockState state, PlayerEntity player,
			ItemStack stack, Hand hand) {
		smother(world, position, state, true);
		player.swingHand(hand);
	}

	private boolean useFuelAndLightWithInteraction(World world, BlockPos position, BlockState state,
			PlayerEntity player,
			ItemStack stack, Hand hand) {
		if (!findAndUseLighterItem(player, hand)) {
			return false;
		}

		light(world, position, state);
		player.swingHand(hand);

		return true;
	}

	// Events

	@Override
	public void onPlaced(World world, BlockPos position, BlockState state, @Nullable LivingEntity placer,
			ItemStack itemStack) {
		super.onPlaced(world, position, state, placer, itemStack);

		BlockEntity blockEntity = world.getBlockEntity(position);

		if (blockEntity != null && blockEntity instanceof FuelBlockEntity && itemStack.getItem() instanceof TorchItem) {
			int fuel = TorchItem.getFuel(itemStack);

			if (fuel == 0) {
				((FuelBlockEntity) blockEntity).setFuel(Mod.config.defaultTorchFuel);
			} else {
				((FuelBlockEntity) blockEntity).setFuel(fuel);
			}
		}
	}

	// Random Tick

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos position, Random random) {
		if (burnState == ETorchState.LIT || burnState == ETorchState.SMOLDERING) {
			TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, position);
		}

		if (burnState == ETorchState.LIT) {
			TorchUtils.displayParticle(this.particle, state, world, position);
		}
	}

	// Modification

	public void modifyTorch(World world, BlockPos position, BlockState curState, ETorchState newType) {
		var blockEntity = (FuelBlockEntity) world.getBlockEntity(position);

		BlockState newBlockState;
		int newFuel = 0;

		if (isWall()) {
			newBlockState = group.getWallTorch(newType).getDefaultState().with(
					HorizontalFacingBlock.FACING,
					curState.get(WallTorchBlock.FACING));
		} else {
			newBlockState = group.getStandingTorch(newType).getDefaultState();
		}

		if (world.getBlockEntity(position) != null) {
			newFuel = blockEntity.getFuel();
		}

		world.setBlockState(position, newBlockState);

		if (world.getBlockEntity(position) != null) {
			blockEntity.setFuel(newFuel);
		}

	}

}
