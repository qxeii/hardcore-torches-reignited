package net.qxeii.hardcore_torches.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.qxeii.hardcore_torches.mixinlogic.InventoryTickMixinLogic;

@Mixin(ServerPlayerEntity.class)
public abstract class InventoryTickMixin implements InventoryTickMixinLogic {
	@Shadow
	public abstract ServerWorld getServerWorld();

	@Shadow
	@Nullable
	private Entity cameraEntity;

	@Inject(at = @At("TAIL"), method = "tick")
	private void tick(CallbackInfo info) {
		ServerWorld world = getServerWorld();
		ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);
		PlayerInventory inventory = player.getInventory();

		if (world.isClient) {
			return;
		}

		for (int i = 0; i < inventory.size(); i++) {
			tickItem(world, player, inventory, i);
		}
	}

}