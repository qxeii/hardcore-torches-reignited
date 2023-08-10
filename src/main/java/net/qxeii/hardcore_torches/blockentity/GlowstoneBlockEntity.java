package net.qxeii.hardcore_torches.blockentity;

import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.block.AbstractGlowstoneBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

import static net.minecraft.util.math.MathHelper.floor;

public class GlowstoneBlockEntity extends FuelBlockEntity {

    public GlowstoneBlockEntity(BlockPos pos, BlockState state) {
        super(Mod.GLOWSTONE_BLOCK_ENTITY, pos, state);
        fuel = Mod.config.defaultGlowstoneFuel;
    }

    public static void tick(World world, BlockPos pos, BlockState state, GlowstoneBlockEntity be) {
        // Burn out
        if (!world.isClient) {
            if (world.getBlockState(pos).getBlock() instanceof AbstractGlowstoneBlock) {
                int newLight = floor((((double) be.fuel / (double) Mod.config.defaultGlowstoneFuel) * 15.0D));
                if (state.get(Properties.LEVEL_15) != newLight) {
                    BlockState newState = state.with(Properties.LEVEL_15, newLight);
                    world.setBlockState(pos, newState);
                }
                if (world.getDimensionKey() == DimensionTypes.THE_NETHER) {
                    if (be.fuel < Mod.config.defaultGlowstoneFuel) {
                        be.fuel++;
                    }
                } else {
                    if (be.fuel > 0) {
                        be.fuel--;
                    }
                }
                be.markDirty();
            }
        }
    }
}