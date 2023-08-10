package net.qxeii.hardcore_torches.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.block.AbstractGlowstoneBlock;
import net.qxeii.hardcore_torches.block.AbstractShroomlightBlock;

import java.util.Random;

import static net.minecraft.util.math.MathHelper.*;
import static net.qxeii.hardcore_torches.util.Helper.getAdjacent;

public class ShroomlightBlockEntity extends FuelBlockEntity {

    public ShroomlightBlockEntity(BlockPos pos, BlockState state) {
        super(Mod.SHROOMLIGHT_BLOCK_ENTITY, pos, state);
        fuel = 0;
    }

    public static void tick(World world, BlockPos pos, BlockState state, ShroomlightBlockEntity be) {
        // Burn out
        if (!world.isClient) {
            if (world.getBlockState(pos).getBlock() instanceof AbstractShroomlightBlock) {
                int newLight = floor((((double) be.fuel / (double) Mod.config.defaultShroomlightFuel) * 15.0D));
                if (state.get(Properties.LEVEL_15) != newLight) {
                    BlockState newState = state.with(Properties.LEVEL_15, newLight);
                    world.setBlockState(pos, newState);
                }
                if (world.getDimensionKey() == DimensionTypes.THE_NETHER) {
                    if (be.fuel < Mod.config.defaultShroomlightFuel) {
                        be.fuel++;
                    }
                } else {
                    if(world.getDimensionKey() == DimensionTypes.OVERWORLD && (world.getTimeOfDay() % 24000) < 13000 &&
                            (getAdjacent(pos).stream().mapToInt(x -> world.getLightLevel(LightType.SKY, x)).sum()/6) > 0)
                    {
                        be.fuel = clamp(be.fuel + (getAdjacent(pos).stream().mapToInt(x -> world.getLightLevel(LightType.SKY, x)).sum()/6), 0,Mod.config.defaultShroomlightFuel);
                    }
                    else {
                        if (be.fuel > 0) {
                            be.fuel --;
                        }
                    }
                }
                be.markDirty();
            }
        }
    }
}