package net.qxeii.hardcore_torches.block;

import java.util.function.IntSupplier;

import net.minecraft.block.Waterloggable;

public class LanternBlock extends AbstractLanternBlock implements Waterloggable {

	public LanternBlock(Settings settings, boolean isLit, IntSupplier maxFuel) {
		super(settings, isLit, maxFuel);
		this.setDefaultState(this.stateManager.getDefaultState().with(HANGING, false).with(WATERLOGGED, false));
	}
}
