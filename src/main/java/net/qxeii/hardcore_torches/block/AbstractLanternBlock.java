package net.qxeii.hardcore_torches.block;

import java.util.function.IntSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
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
import net.minecraft.registry.tag.ItemTags;
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
import net.qxeii.hardcore_torches.blockentity.IFuelBlock;
import net.qxeii.hardcore_torches.blockentity.LanternBlockEntity;
import net.qxeii.hardcore_torches.item.LanternItem;
import net.qxeii.hardcore_torches.item.OilCanItem;
import net.qxeii.hardcore_torches.util.TorchUtils;

public abstract class AbstractLanternBlock extends BlockWithEntity implements BlockEntityProvider, IFuelBlock {
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

	public boolean canLight(World world, BlockPos pos) {
		return ((LanternBlockEntity) world.getBlockEntity(pos)).getFuel() > 0 && !isLit;
	}

	public void extinguish(World world, BlockPos pos, BlockState state, boolean playSound) {
		if (!world.isClient) {
			if (playSound)
				world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
			TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
			TorchUtils.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
			TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, pos);
			TorchUtils.displayParticle(ParticleTypes.SMOKE, state, world, pos);
			setState(world, pos, false);
		}
	}

	public void light(World world, BlockPos pos, BlockState state) {
		if (!world.isClient) {
			world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 2, 1);
			world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 2, 2);
			world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1f, 1f);
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

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		ItemStack stack = player.getStackInHand(hand);
		BlockEntity be = world.getBlockEntity(pos);
		boolean success = false;

		// Pick up lantern
		if (player.isSneaking() && Mod.config.pickUpLanterns) {
			if (!world.isClient)
				player.giveItemStack(getStack(world, pos));
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			if (!world.isClient)
				world.playSound(null, pos, SoundEvents.BLOCK_LANTERN_HIT, SoundCategory.BLOCKS, 1f, 1f);
			player.swingHand(hand);
			return ActionResult.SUCCESS;
		}

		// Igniting
		if (!this.isLit && itemValid(stack, Mod.FREE_LANTERN_LIGHT_ITEMS, Mod.DAMAGE_LANTERN_LIGHT_ITEMS,
				Mod.CONSUME_LANTERN_LIGHT_ITEMS)) {

			// If not enough fuel to light
			if (((FuelBlockEntity) world.getBlockEntity(pos)).getFuel() < Mod.config.minLanternIgnitionFuel) {
				if (!world.isClient) {
					world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 2, 2);
					player.sendMessage(MutableText.of(new LiteralTextContent("Not enough fuel to ignite!")), true);
				}
				player.swingHand(hand);
				return ActionResult.SUCCESS;
			}

			if (attemptUse(stack, player, hand, Mod.FREE_LANTERN_LIGHT_ITEMS, Mod.DAMAGE_LANTERN_LIGHT_ITEMS,
					Mod.CONSUME_LANTERN_LIGHT_ITEMS)) {
				light(world, pos, state);
				player.swingHand(hand);
				return ActionResult.SUCCESS;
			}
		}

		// Adding fuel
		if (stack.isIn(ItemTags.COALS) && !Mod.config.lanternsNeedCan) {
			if (be instanceof FuelBlockEntity) {
				int oldFuel = ((FuelBlockEntity) be).getFuel();

				if (oldFuel < Mod.config.defaultLanternFuel) {
					if (oldFuel + Mod.config.defLanternFuelItem < Mod.config.defaultLanternFuel) {
						world.playSound(null, pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
								SoundCategory.BLOCKS, 1f, 0f);
						world.playSound(null, pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
								SoundCategory.BLOCKS, 1f, 2f);
					} else {
						if (!world.isClient)
							world.playSound(null, pos, SoundEvents.BLOCK_HONEY_BLOCK_STEP, SoundCategory.BLOCKS, 1f,
									0f);
					}

					stack.decrement(1);
					((FuelBlockEntity) be)
							.setFuel(Math.min(oldFuel + Mod.config.defLanternFuelItem, Mod.config.defaultLanternFuel));
					player.swingHand(hand);
					return ActionResult.SUCCESS;
				}
			}
		}

		// Adding fuel with can
		if (stack.getItem() instanceof OilCanItem && Mod.config.lanternsNeedCan) {
			if (be instanceof FuelBlockEntity && !world.isClient) {
				if (OilCanItem.fuelBlock((FuelBlockEntity) be, world, stack)) {
					world.playSound(null, pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
							SoundCategory.BLOCKS, 1f, 0f);
					world.playSound(null, pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
							SoundCategory.BLOCKS, 1f, 2f);
					world.playSound(null, pos, SoundEvents.ITEM_HONEY_BOTTLE_DRINK, SoundCategory.BLOCKS, 0.3f, 0f);
				}
			}
			player.swingHand(hand);
			return ActionResult.SUCCESS;
		}

		boolean showFuel = (stack.isEmpty() || stack.getItem() == Mod.OIL_CAN) && Mod.config.fuelMessage;

		// Fuel message
		if (be.getType() == Mod.LANTERN_BLOCK_ENTITY && !world.isClient && hand == Hand.MAIN_HAND
				&& Mod.config.fuelMessage && showFuel) {
			player.sendMessage(MutableText.of(new LiteralTextContent("Fuel: " + ((FuelBlockEntity) be).getFuel())),
					true);
		}

		if (Mod.config.lanternsNeedCan && !stack.isEmpty() && hand == Hand.MAIN_HAND && stack.getItem() != Mod.OIL_CAN
				&& !world.isClient) {
			player.sendMessage(MutableText.of(new LiteralTextContent("Requires an Oil Canister to fuel!")), true);
			world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 2, 2);
		}

		// Hand extinguish
		if (Mod.config.handUnlightLantern && isLit) {
			if (!TorchUtils.canLight(stack.getItem(), state)) {
				extinguish(world, pos, state, true);
				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.PASS;
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
			ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);

		BlockEntity be = world.getBlockEntity(pos);

		((FuelBlockEntity) be).setFuel(0);

		if (be != null && be instanceof FuelBlockEntity && itemStack.getItem() instanceof LanternItem) {
			int fuel = LanternItem.getFuel(itemStack);

			((FuelBlockEntity) be).setFuel(fuel);
		}
	}

	public static boolean isLightItem(ItemStack stack) {
		if (stack.isIn(Mod.FREE_LANTERN_LIGHT_ITEMS))
			return true;
		if (stack.isIn(Mod.DAMAGE_LANTERN_LIGHT_ITEMS))
			return true;
		if (stack.isIn(Mod.CONSUME_LANTERN_LIGHT_ITEMS))
			return true;
		return false;
	}

	// region IFuelBlock
	@Override
	public void outOfFuel(World world, BlockPos pos, BlockState state, boolean playSound) {
		((AbstractLanternBlock) world.getBlockState(pos).getBlock()).extinguish(world, pos, state, playSound);
	}
	// endregion

	// region Overridden methods for LanternBlock since I can't extend 2 classes
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
		if (state != null)
			newState = getDefaultState().with(HANGING, state.get(HANGING)).with(WATERLOGGED, state.get(WATERLOGGED));
		return newState;
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
			WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		// Lantern uses a hanging property we don't have.
		// return getDefaultState();
		return Blocks.LANTERN.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return Blocks.LANTERN.canPlaceAt(state, world, pos);
		// return true;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(new Property[] { HANGING, WATERLOGGED });
	}
	// endregion

	// region BlockEntity code
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LanternBlockEntity(pos, state);
	}

	// Is invisible without this
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		// With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need
		// to change that!
		return BlockRenderType.MODEL;
	}

	// Needed for ticking, idk what it means
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return checkType(type, Mod.LANTERN_BLOCK_ENTITY,
				(world1, pos, state1, be) -> LanternBlockEntity.tick(world1, pos, state1, be));
	}
	// endregion
}
