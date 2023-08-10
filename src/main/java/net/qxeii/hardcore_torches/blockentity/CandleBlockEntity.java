package net.qxeii.hardcore_torches.blockentity;

import net.qxeii.hardcore_torches.Mod;
import net.qxeii.hardcore_torches.block.AbstractHardcoreCandleBlock;
import net.qxeii.hardcore_torches.block.AbstractHardcoreTorchBlock;
import net.qxeii.hardcore_torches.block.CandleBlock;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.spi.LoggerRegistry;

import java.util.logging.Logger;

public class CandleBlockEntity extends FuelBlockEntity {

    public CandleBlockEntity(BlockPos pos, BlockState state) {
        super(Mod.CANDLE_BLOCK_ENTITY, pos, state);
        fuel = Mod.config.defaultCandleFuel;
    }

    public static void tick(World world, BlockPos pos, BlockState state, CandleBlockEntity be) {
        if (!world.isClient) {
            // Burn out
            if (world.getBlockState(pos).getBlock() instanceof AbstractHardcoreCandleBlock) {
                if (be.fuel > 0 && AbstractHardcoreCandleBlock.isLitCandle(state)) {
                    be.fuel--;

                    if (be.fuel <= 0) {
                        ((AbstractHardcoreCandleBlock) world.getBlockState(pos).getBlock()).outOfFuel(world, pos, state, false);
                    }
                }

                be.markDirty();
            }
        }
    }
}
