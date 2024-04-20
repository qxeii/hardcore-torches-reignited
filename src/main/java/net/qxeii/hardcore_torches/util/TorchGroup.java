package net.qxeii.hardcore_torches.util;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.qxeii.hardcore_torches.block.FloorTorchBlock;
import net.qxeii.hardcore_torches.block.WallTorchBlock;

public class TorchGroup {

	private HashMap<ETorchState, FloorTorchBlock> standingTorches = new HashMap<ETorchState, FloorTorchBlock>();
	private HashMap<ETorchState, WallTorchBlock> wallTorches = new HashMap<ETorchState, WallTorchBlock>();
	public final String name;

	public TorchGroup(String name) {
		this.name = name;
	}

	public void add(Block block) {
		if (block instanceof FloorTorchBlock) {
			add((FloorTorchBlock) block);
		} else if (block instanceof WallTorchBlock) {
			add((WallTorchBlock) block);
		}
	}

	public void add(FloorTorchBlock block) {
		standingTorches.put(block.burnState, block);
		block.group = this;
	}

	public void add(WallTorchBlock block) {
		wallTorches.put(block.burnState, block);
		block.group = this;
	}

	public FloorTorchBlock getStandingTorch(ETorchState state) {
		return standingTorches.get(state);
	}

	public WallTorchBlock getWallTorch(ETorchState state) {
		return wallTorches.get(state);
	}
}
