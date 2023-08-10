package net.qxeii.hardcore_torches.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.LightType;
import net.minecraft.world.dimension.DimensionTypes;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.item.GlowstoneItem;
import net.qxeii.hardcore_torches.item.LanternItem;
import net.qxeii.hardcore_torches.item.ShroomlightItem;
import net.qxeii.hardcore_torches.item.TorchItem;
import net.qxeii.hardcore_torches.util.ETorchState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
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
        if (!this.getServerWorld().isClient) {
            ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);

            PlayerInventory inventory = player.getInventory(); //idek man, Commobile told me to do it

            for (int i = 0; i < inventory.offHand.size(); i++) {
                tickTorch(inventory.offHand.get(i), inventory, i, inventory.offHand);
            }

            for (int i = 0; i < inventory.main.size(); i++) {
                tickTorch(inventory.main.get(i), inventory, i, inventory.main);
            }

            waterCheck(player, inventory);
        }
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
        if (player.isSubmergedInWater()) {
            if (torchItem.getTorchState() == ETorchState.LIT || torchItem.getTorchState() == ETorchState.SMOLDERING) {
                if ((Mod.config.invExtinguishInWater == 1 && mainOrOffhand) || Mod.config.invExtinguishInWater == 2) {
                    player.getInventory().setStack(i, TorchItem.stateStack(stack, ETorchState.UNLIT));
                    player.getWorld().playSound(null, pos.up(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1f);
                }
            }
        }
    }

    private void rainTorch(int i, TorchItem torchItem, ItemStack stack, PlayerInventory inventory, World world, BlockPos pos) {
        if (torchItem.getTorchState() == ETorchState.LIT) {
            if (Mod.config.torchesSmolder) {
                inventory.setStack(i, TorchItem.stateStack(stack, ETorchState.SMOLDERING));
                world.playSound(null, pos.up(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1f);
            } else {
                inventory.setStack(i, TorchItem.stateStack(stack, ETorchState.UNLIT));
                world.playSound(null, pos.up(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1f);
            }
        } else if (torchItem.getTorchState() == ETorchState.SMOLDERING) {
            if (!Mod.config.torchesSmolder) {
                inventory.setStack(i, TorchItem.stateStack(stack, ETorchState.UNLIT));
                world.playSound(null, pos.up(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1f);
            }
        }
    }

    private void tickTorch(ItemStack stack, PlayerInventory inventory, int index, DefaultedList<ItemStack> list) {
        Item item = stack.getItem();

        if (item instanceof LanternItem && ((LanternItem) item).isLit) {
            if (Mod.config.tickInInventory) list.set(index, LanternItem.addFuel(stack, this.getServerWorld(), -1));
        }

        if (item instanceof ShroomlightItem) {
            if (Mod.config.tickInInventory) {
                if (this.getServerWorld().getDimensionKey().equals(DimensionTypes.THE_NETHER)) {
                    list.set(index, ShroomlightItem.addFuel(stack, this.getServerWorld(), 15));
                } else {
                    if (((this.getServerWorld().getTimeOfDay() % 24000)< 13000)) {
                        list.set(index, ShroomlightItem.addFuel(stack, this.getServerWorld(), -1));
                    }
                }
            }
        }

        if (item instanceof GlowstoneItem) {
            if (Mod.config.tickInInventory) {
                if (this.getServerWorld().getDimensionKey().equals(DimensionTypes.THE_NETHER)) {
                    list.set(index, GlowstoneItem.addFuel(stack, this.getServerWorld(), 15));
                }
                else
                {
                    list.set(index, ShroomlightItem.addFuel(stack, this.getServerWorld(), -1));
                }
            }
        }


        if (item instanceof TorchItem) {
            ETorchState state = ((TorchItem) item).getTorchState();

            if (state == ETorchState.LIT) {
                if (Mod.config.tickInInventory) list.set(index, TorchItem.addFuel(stack, this.getServerWorld(),-1));
            } else if (state == ETorchState.SMOLDERING) {
                if (Mod.config.tickInInventory && random.nextInt(3) == 0) list.set(index, TorchItem.addFuel(stack, this.getServerWorld(),-1));
            }
        }
    }
}
