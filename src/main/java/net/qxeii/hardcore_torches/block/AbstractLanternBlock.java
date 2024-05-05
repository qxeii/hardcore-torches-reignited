package net.qxeii.hardcore_torches.block;

import java.util.function.IntSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.blockentity.FuelBlockEntity;
import net.qxeii.hardcore_torches.blockentity.LanternBlockEntity;
import net.qxeii.hardcore_torches.blockentity.LightableBlock;
import net.qxeii.hardcore_torches.item.FuelCanItem;
import net.qxeii.hardcore_torches.item.LanternItem;
import net.qxeii.hardcore_torches.util.TorchUtils;
import net.qxeii.hardcore_torches.util.WorldUtils;

public abstract class AbstractLanternBlock extends BlockWithEntity implements LightableBlock {
	public static final BooleanProperty HANGING;
	public static final BooleanProperty WATERLOGGED;

	public boolean isLit;

	public IntSupplier maxFuel;

	static {
		HANGING = Properties.HANGING;
		WATERLOGGED = Properties.WATERLOGGED;
	}

	protected AbstractLanternBlock(Settings settings, boolean isLit, IntSupplier maxFuel) {
		super(settings);
		this.isLit = isLit;
		this.maxFuel = maxFuel;
	}

	public void setState(World world, BlockPos position, boolean lit) {
		var currentBlockState = world.getBlockState(position);
		var currentBlockEntity = (FuelBlockEntity) world.getBlockEntity(position);
		var newBlockState = lit ? Mod.LIT_LANTERN.getDefaultState() : Mod.UNLIT_LANTERN.getDefaultState();

		newBlockState = newBlockState.with(HANGING, currentBlockState.get(HANGING)).with(WATERLOGGED,
				currentBlockState.get(WATERLOGGED));

		int fuel = currentBlockEntity.getFuel();

		world.setBlockState(position, newBlockState);

		var newBlockEntity = (FuelBlockEntity) world.getBlockEntity(position);
		newBlockEntity.setFuel(fuel);
	}

	protected ItemStack getStack(World world, BlockPos pos) {
		ItemStack stack = new ItemStack(world.getBlockState(pos).getBlock().asItem());
		BlockEntity blockEntity = world.getBlockEntity(pos);
		int remainingFuel;

		// Set fuel
		if (blockEntity != null && blockEntity instanceof FuelBlockEntity) {
			remainingFuel = ((FuelBlockEntity) blockEntity).getFuel();
			NbtCompound nbt = new NbtCompound();
			nbt.putInt("Fuel", (remainingFuel));
			stack.setNbt(nbt);
		}

		return stack;
	}

	// Actions

	public void extinguish(World world, BlockPos position, BlockState state, boolean playSound) {
		if (world.isClient) {
			return;
		}

		if (playSound) {
			world.playSound(null, position, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 0.5f);
			world.playSound(null, position, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1f, 0.5f);
		}

		TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, position);
		TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, position);
		TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, position);
		TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, position);

		setState(world, position, false);
	}

	public void light(World world, BlockPos position, BlockState state) {
		if (world.isClient) {
			return;
		}

		world.playSound(null, position, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 2, 1);
		world.playSound(null, position, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 2, 2);
		world.playSound(null, position, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1f, 0.5f);
		world.playSound(null, position, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 2f, 2f);
		world.playSound(null, position, SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 2f, 2f);
		world.playSound(null, position, SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 2f, 2f);

		ServerWorld serverWorld = (ServerWorld) world;

		serverWorld.spawnParticles(ParticleTypes.SMOKE, position.getX() + 0.5, position.getY() + 0.5,
				position.getZ() + 0.5, 10,
				0.15, 0.15, 0.15, 0.001);
		serverWorld.spawnParticles(ParticleTypes.LAVA, position.getX() + 0.5, position.getY() + 0.5,
				position.getZ() + 0.5, 2,
				0.15, 0.15, 0.15, 0.001);

		setState(world, position, true);
	}

	// Interaction

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos position, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		ItemStack stack = player.getStackInHand(hand);

		// Adding fuel with can
		if (stack.getItem() instanceof FuelCanItem) {
			refuelWithInteraction(world, position, state, player, stack, hand);
			return ActionResult.SUCCESS;
		}

		// Pick up lantern
		if (stack.isEmpty() && player.isSneaking()) {
			if (Mod.config.fuelMessage && !world.isClient) {
				var blockEntity = (FuelBlockEntity) world.getBlockEntity(position);
				displayFuelMessage(world, player, blockEntity);
			}

			return ActionResult.SUCCESS;
		}

		// Igniting
		if (stack.isEmpty() && !this.isLit) {
			useFuelAndLightWithInteraction(state, world, position, player, hand);
			return ActionResult.SUCCESS;
		}

		// Extinguishing
		if (stack.isEmpty() && this.isLit) {
			extinguishWithInteraction(world, position, state, player, hand);
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	private void pickUp(World world, BlockPos position, PlayerEntity player, Hand hand) {
		if (!world.isClient) {
			player.giveItemStack(getStack(world, position));
		}

		world.setBlockState(position, Blocks.AIR.getDefaultState());

		if (!world.isClient) {
			world.playSound(null, position, SoundEvents.BLOCK_LANTERN_HIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
		}
	}

	public void useFuelAndLightWithInteraction(BlockState state, World world, BlockPos position, PlayerEntity player,
			Hand hand) {
		FuelBlockEntity blockEntity = (FuelBlockEntity) world.getBlockEntity(position);

		// If not enough fuel to light
		if (blockEntity.getFuel() < Mod.config.minLanternIgnitionFuel) {
			if (!world.isClient) {
				world.playSound(null, position, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0f, 2.0f);
			}

			return;
		}

		blockEntity.modifyFuel(-Mod.config.lanternLightFuelLoss);

		light(world, position, state);

		if (Mod.config.fuelMessage && !world.isClient) {
			displayFuelMessage(world, player, blockEntity);
		}
	}

	public void extinguishWithInteraction(World world, BlockPos position, BlockState state, PlayerEntity player,
			Hand hand) {
		extinguish(world, position, state, true);
	}

	public void refuelWithInteraction(World world, BlockPos position, BlockState state, PlayerEntity player,
			ItemStack stack,
			Hand hand) {
		FuelBlockEntity blockEntity = (FuelBlockEntity) world.getBlockEntity(position);

		if (FuelCanItem.fuelBlock((FuelBlockEntity) blockEntity, world, stack)) {
			world.playSound(null, position, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
					SoundCategory.BLOCKS, 1f, 0f);
			world.playSound(null, position, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
					SoundCategory.BLOCKS, 1f, 2f);
			world.playSound(null, position, SoundEvents.ITEM_HONEY_BOTTLE_DRINK, SoundCategory.BLOCKS, 0.3f, 0f);
		}
	}

	private void displayFuelMessage(World world, PlayerEntity player, FuelBlockEntity blockEntity) {
		var fuel = blockEntity.getFuel();
		var fuelText = WorldUtils.formattedFuelText(fuel);

		player.sendMessage(fuelText, true);
	}

	// Events

	@Override
	public void onPlaced(World world, BlockPos position, BlockState state, @Nullable LivingEntity placer,
			ItemStack itemStack) {
		super.onPlaced(world, position, state, placer, itemStack);

		var blockEntity = (FuelBlockEntity) world.getBlockEntity(position);
		var fuel = LanternItem.getFuel(itemStack);

		blockEntity.setFuel(fuel);
	}

	// Utility

	public boolean canLight(World world, BlockPos position) {
		return ((LanternBlockEntity) world.getBlockEntity(position)).getFuel() > 0 && !isLit;
	}

	@Override
	public void onOutOfFuel(World world, BlockPos position, BlockState state, boolean playSound) {
		((AbstractLanternBlock) world.getBlockState(position).getBlock()).extinguish(world, position, state, playSound);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos position, ShapeContext context) {
		return this.getOutlineShape(state, world, position, context);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos position, ShapeContext context) {
		// Lantern uses a hanging property we don't have meaning I just copy it
		// return VoxelShapes.union(Block.createCuboidShape(5.0D, 0.0D, 5.0D, 11.0D,
		// 7.0D, 11.0D), Block.createCuboidShape(6.0D, 7.0D, 6.0D, 10.0D, 9.0D, 10.0D));
		return Blocks.LANTERN.getOutlineShape(state, world, position, context);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState state = Blocks.LANTERN.getPlacementState(ctx);
		BlockState newState = null;

		if (state != null) {
			newState = getDefaultState().with(HANGING, state.get(HANGING)).with(WATERLOGGED, state.get(WATERLOGGED));
		}

		return newState;
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
			WorldAccess world, BlockPos position, BlockPos neighborPosition) {
		return Blocks.LANTERN.getStateForNeighborUpdate(state, direction, neighborState, world, position,
				neighborPosition);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos position) {
		return Blocks.LANTERN.canPlaceAt(state, world, position);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(new Property[] { HANGING, WATERLOGGED });
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos position, BlockState state) {
		return new LanternBlockEntity(position, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		// With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need
		// to change that!
		return BlockRenderType.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return checkType(type, Mod.LANTERN_BLOCK_ENTITY,
				(_world, position, _state, be) -> LanternBlockEntity.tick(_world, position, _state, be));
	}

}
