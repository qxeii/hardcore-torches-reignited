package net.qxeii.hardcore_torches.blockentity;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class FuelBlockEntity extends BlockEntity {

	public static Random random = new Random();

	private int fuel;

	public FuelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	// Serialize the BlockEntity
	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);

		// Save the current value of the number to the tag
		tag.putInt("Fuel", fuel);
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	// Deserialize the BlockEntity
	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);

		if (tag.contains("number")) {
			fuel = tag.getInt("number");
		} else {
			fuel = tag.getInt("Fuel");
		}
	}

	// Fuel Access

	public boolean isOutOfFuel() {
		return fuel <= 0;
	}

	public int getFuel() {
		return fuel;
	}

	public void setFuel(int newValue) {
		fuel = Math.max(0, newValue);
	}

	public void modifyFuel(int increment) {
		setFuel(fuel + increment);
	}
}
