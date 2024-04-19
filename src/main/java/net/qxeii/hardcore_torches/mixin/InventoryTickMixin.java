package net.qxeii.hardcore_torches.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.world.LightType;
import net.minecraft.world.dimension.DimensionTypes;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.item.GlowstoneItem;
import net.qxeii.hardcore_torches.item.LanternItem;
import net.qxeii.hardcore_torches.item.ShroomlightItem;
import net.qxeii.hardcore_torches.item.TorchItem;
import net.qxeii.hardcore_torches.util.ETorchState;
import net.qxeii.hardcore_torches.util.WorldUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ServerPlayerEntity.class)
public abstract class InventoryTickMixin {
    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow @Nullable private Entity cameraEntity;
    private static Random random = new Random();

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {
        if (this.getServerWorld().isClient) {
            return;
        }

        ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.size(); i++) {
            tickTorch(inventory, i);
        }

        waterCheck(player, inventory);
    }

    private void waterCheck(ServerPlayerEntity player, PlayerInventory inventory) {
        BlockPos pos = player.getBlockPos();
        int rainEffect = Mod.config.invExtinguishInRain;
        boolean doRain = rainEffect > 0 && player.getWorld().hasRain(pos);

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            Item item = stack.getItem();

            // Torches
            if (item instanceof TorchItem) {
                TorchItem torchItem = (TorchItem) item;
                boolean rain = doRain;
                boolean mainOrOffhand = (i == inventory.selectedSlot || inventory.offHand.get(0) == stack);
                if (rainEffect == 1 && doRain) rain = mainOrOffhand ? true : false;

                // Rain
                if (rain) rainTorch(i, torchItem, stack, inventory, player.getWorld(), pos);

                // Underwater
                if (Mod.config.invExtinguishInWater > 0) waterTorch(i, torchItem, stack, player, mainOrOffhand, pos);
            }
        }
    }

    private void waterTorch(int i, TorchItem torchItem, ItemStack stack, ServerPlayerEntity player, boolean mainOrOffhand, BlockPos pos) {
        if (!player.isSubmergedInWater()) {
            return;
        }

        if (!((Mod.config.invExtinguishInWater == 1 && mainOrOffhand) || Mod.config.invExtinguishInWater == 2)) {
            return;
        }

        ETorchState torchState = torchItem.getTorchState();
        int torchConditionLoss = torchState == ETorchState.LIT ? -Mod.config.torchesExtinguishConditionLoss : 0;

        if (torchState != ETorchState.LIT && torchState != ETorchState.SMOLDERING) {
            return;
        }

        ItemStack torchStack = stack;
        torchStack = TorchItem.stateStack(torchStack, ETorchState.UNLIT);
        torchStack = TorchItem.addFuel(torchStack, player.getWorld(), torchConditionLoss);
        player.getInventory().setStack(i, torchStack);
        
        player.getWorld().playSound(null, pos.up(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1f);
    }

    private void rainTorch(int i, TorchItem torchItem, ItemStack stack, PlayerInventory inventory, World world, BlockPos pos) {
        ETorchState torchState = torchItem.getTorchState();

        if (torchState == ETorchState.UNLIT) {
            return;
        }

        ETorchState targetTorchState = torchState == ETorchState.LIT && Mod.config.torchesSmolder ? ETorchState.SMOLDERING : ETorchState.UNLIT;
        int torchConditionLoss = torchState == ETorchState.LIT ? -Mod.config.torchesExtinguishConditionLoss : 0;

        ItemStack torchStack = stack;
        torchStack = TorchItem.stateStack(torchStack, targetTorchState);
        torchStack = TorchItem.addFuel(torchStack, world, torchConditionLoss);
        inventory.setStack(i, torchStack);

        world.playSound(null, pos.up(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1f);
    }

    private void tickTorch(PlayerInventory inventory, int slot) {
        if (!Mod.config.tickInInventory) {
            return;
        }

        ItemStack stack = inventory.getStack(slot);
        Item item = stack.getItem();

        // Pre-Pass: Convert vanilla torches

        if (Mod.config.convertVanillaTorches && item == Items.TORCH) {
            convertVanillaTorch(stack, inventory, slot);
            return;
        }

        // Main Pass: Check torch type, burn fuel

        ServerWorld world = getServerWorld();
        boolean worldIsNether = WorldUtils.worldIsNether(world);
        boolean worldTimeIsDay = WorldUtils.worldIsDaytime(world);

        int itemFuelUse = 1;

        // If player actively holds torch (as dynamic light), multiply fuel use
        if (slot == inventory.selectedSlot || slot == PlayerInventory.OFF_HAND_SLOT) {
            itemFuelUse = itemFuelUse * Mod.config.torchFuelUseMultiplierWhenHeld;
        }

        if (item instanceof LanternItem && ((LanternItem) item).isLit) {
            ItemStack modifiedStack = LanternItem.addFuel(stack, world, -itemFuelUse);
            inventory.setStack(slot, modifiedStack);
            return;
        }

        if (item instanceof ShroomlightItem) {
            if (worldIsNether) {
                ItemStack modifiedStack = ShroomlightItem.addFuel(stack, world, 15);
                inventory.setStack(slot, modifiedStack);

                return;
            }
            
            if (worldTimeIsDay) {
                ItemStack modifiedStack = ShroomlightItem.addFuel(stack, world, -itemFuelUse);
                inventory.setStack(slot, modifiedStack);

                return;
            }

            return;
        }

        if (item instanceof GlowstoneItem) {
            if (worldIsNether) {
                ItemStack modifiedStack = GlowstoneItem.addFuel(stack, world, 15);
                inventory.setStack(slot, modifiedStack);

                return;
            }

            ItemStack modifiedStack = ShroomlightItem.addFuel(stack, world, -itemFuelUse);
            inventory.setStack(slot, modifiedStack);

            return;
        }

        if (item instanceof TorchItem) {
            ETorchState state = ((TorchItem) item).getTorchState();

            if (state == ETorchState.LIT) {
                ItemStack modifiedStack = TorchItem.addFuel(stack, world, -itemFuelUse);
                inventory.setStack(slot, modifiedStack);
            } else if (state == ETorchState.SMOLDERING) {
                if(random.nextInt(Mod.config.torchesSmolderFuelUseTickChance) == 0) {
                    ItemStack modifiedStack = TorchItem.addFuel(stack, world, -itemFuelUse);
                    inventory.setStack(slot, modifiedStack);
                } else if (random.nextInt(Mod.config.torchesSmolderExtinguishTickChance) == 0) {
                    ItemStack modifiedStack = TorchItem.stateStack(stack, ETorchState.UNLIT);
                    inventory.setStack(slot, modifiedStack);
                }
            }

            return;
        }
    }

    private void convertVanillaTorch(ItemStack stack, PlayerInventory inventory, int index) {
        // Remove currently held vanilla torch stack and give a stack of same quantity of mod torches.
        Block torchItem = Mod.config.torchesBurnWhenConverted ? Mod.LIT_TORCH : Mod.UNLIT_TORCH;
        inventory.setStack(index, new ItemStack(torchItem, stack.getCount()));
    }
}