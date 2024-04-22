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
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
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
import net.qxeii.hardcore_torches.item.LanternItem;
import net.qxeii.hardcore_torches.item.OilCanItem;
import net.qxeii.hardcore_torches.util.TorchUtils;

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

	public void setState(World world, BlockPos pos, boolean lit) {
		BlockState oldState = world.getBlockState(pos);
		BlockState newState = lit ? Mod.LIT_LANTERN.getDefaultState() : Mod.UNLIT_LANTERN.getDefaultState();
		newState = newState.with(HANGING, oldState.get(HANGING)).with(WATERLOGGED, oldState.get(WATERLOGGED));
		int newFuel = Mod.config.startingLanternFuel;

		if (world.getBlockEntity(pos) != null)
			newFuel = ((FuelBlockEntity) world.getBlockEntity(pos)).getFuel();
		world.setBlockState(pos, newState);
		if (world.getBlockEntity(pos) != null)
			((FuelBlockEntity) world.getBlockEntity(pos)).setFuel(newFuel);
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

	public void extinguish(World world, BlockPos pos, BlockState state, boolean playSound) {
		if (!world.isClient) {
			if (playSound) {
				world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 0.5f);
				world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1f, 0.5f);
			}

			TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
			TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
			TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, pos);
			TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, pos);

			setState(world, pos, false);
		}
	}

	public void light(World world, BlockPos pos, BlockState state) {
		if (world.isClient) {
			return;
		}

		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 2, 1);
		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 2, 2);
		world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1f, 0.5f);
		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 2f, 2f);
		world.playSound(null, pos, SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 2f, 2f);
		world.playSound(null, pos, SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 2f, 2f);

		ServerWorld serverWorld = (ServerWorld) world;

		serverWorld.spawnParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10,
				0.15, 0.15, 0.15, 0.001);
		serverWorld.spawnParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2,
				0.15, 0.15, 0.15, 0.001);

		setState(world, pos, true);
	}

	// Interaction

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		ItemStack stack = player.getStackInHand(hand);

		// Pick up lantern
		if (player.isSneaking() && Mod.config.pickUpLanterns) {
			pickUp(world, pos, player, hand);
			return ActionResult.SUCCESS;
		}

		// Adding fuel with can
		if (stack.getItem() instanceof OilCanItem) {
			refuelWithInteraction(world, pos, state, player, stack, hand);
			return ActionResult.SUCCESS;
		}

		// Igniting
		if (!this.isLit && stack.isEmpty()) {
			useFuelAndLightWithInteraction(state, world, pos, player, hand);
			return ActionResult.SUCCESS;
		}

		// Extinguishing
		if (this.isLit && stack.isEmpty()) {
			extinguishWithInteraction(world, pos, state, player, hand);
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	private void pickUp(World world, BlockPos pos, PlayerEntity player, Hand hand) {
		if (!world.isClient) {
			player.giveItemStack(getStack(world, pos));
		}

		world.setBlockState(pos, Blocks.AIR.getDefaultState());

		if (!world.isClient) {
			world.playSound(null, pos, SoundEvents.BLOCK_LANTERN_HIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
		}

		player.swingHand(hand);
	}

	public void useFuelAndLightWithInteraction(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand hand) {
		FuelBlockEntity blockEntity = (FuelBlockEntity) world.getBlockEntity(pos);

		// If not enough fuel to light
		if (blockEntity.getFuel() < Mod.config.minLanternIgnitionFuel) {
			if (!world.isClient) {
				world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0f, 2.0f);
			}

			player.swingHand(hand);
			return;
		}

		light(world, pos, state);
		player.swingHand(hand);

		blockEntity.modifyFuel(-Mod.config.lanternLightFuelLoss);

		if (Mod.config.fuelMessage && !world.isClient && hand == Hand.MAIN_HAND) {
			player.sendMessage(MutableText.of(new LiteralTextContent("Fuel: " + blockEntity.getFuel())), true);
		}
	}

	public void extinguishWithInteraction(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand) {
		extinguish(world, pos, state, true);
		player.swingHand(hand);
	}

	public void refuelWithInteraction(World world, BlockPos pos, BlockState state, PlayerEntity player,
			ItemStack stack,
			Hand hand) {
		FuelBlockEntity blockEntity = (FuelBlockEntity) world.getBlockEntity(pos);

		if (OilCanItem.fuelBlock((FuelBlockEntity) blockEntity, world, stack)) {
			world.playSound(null, pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
					SoundCategory.BLOCKS, 1f, 0f);
			world.playSound(null, pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
					SoundCategory.BLOCKS, 1f, 2f);
			world.playSound(null, pos, SoundEvents.ITEM_HONEY_BOTTLE_DRINK, SoundCategory.BLOCKS, 0.3f, 0f);
		}

		player.swingHand(hand);
	}

	// Events

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
			ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);

		BlockEntity be = world.getBlockEntity(pos);

		// Temporarily disabled for testing, placed lanterns should not have zero fuel.
		// ((FuelBlockEntity) be).setFuel(0);

		if (be != null && be instanceof FuelBlockEntity && itemStack.getItem() instanceof LanternItem) {
			int fuel = LanternItem.getFuel(itemStack);

			((FuelBlockEntity) be).setFuel(fuel);
		}
	}

	// Utility

	public boolean canLight(World world, BlockPos pos) {
		return ((LanternBlockEntity) world.getBlockEntity(pos)).getFuel() > 0 && !isLit;
	}

	@Override
	public void onOutOfFuel(World world, BlockPos pos, BlockState state, boolean playSound) {
		((AbstractLanternBlock) world.getBlockState(pos).getBlock()).extinguish(world, pos, state, playSound);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return this.getOutlineShape(state, world, pos, context);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		// Lantern uses a hanging property we don't have meaning I just copy it
		// return VoxelShapes.union(Block.createCuboidShape(5.0D, 0.0D, 5.0D, 11.0D,
		// 7.0D, 11.0D), Block.createCuboidShape(6.0D, 7.0D, 6.0D, 10.0D, 9.0D, 10.0D));
		return Blocks.LANTERN.getOutlineShape(state, world, pos, context);
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
			WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		return Blocks.LANTERN.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return Blocks.LANTERN.canPlaceAt(state, world, pos);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(new Property[] { HANGING, WATERLOGGED });
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LanternBlockEntity(pos, state);
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
				(world1, pos, state1, be) -> LanternBlockEntity.tick(world1, pos, state1, be));
	}

}
