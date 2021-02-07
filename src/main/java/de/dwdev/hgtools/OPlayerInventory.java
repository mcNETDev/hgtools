package de.dwdev.hgtools;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class OPlayerInventory implements IInventory {

	private ServerPlayerEntity player;

	public OPlayerInventory(ServerPlayerEntity playerSeenIn) {
		this.player = playerSeenIn;
	}

	@Override
	public void clear() {
		player.inventory.clear();
	}

	@Override
	public int getSizeInventory() {
		return 45;
	}

	@Override
	public boolean isEmpty() {
		return player.inventory.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (invalid(index)) {
			return ItemStack.EMPTY;
		}

		int slot = getIndex(index);
		return slot == -1 ? ItemStack.EMPTY : player.inventory.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (invalid(index)) {
			return ItemStack.EMPTY;
		}

		int slot = getIndex(index);
		return slot == -1 ? ItemStack.EMPTY : player.inventory.decrStackSize(slot, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (invalid(index)) {
			return ItemStack.EMPTY;
		}

		int slot = getIndex(index);
		return slot == -1 ? ItemStack.EMPTY : player.inventory.removeStackFromSlot(slot);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (invalid(index)) {
			return;
		}

		int slot = getIndex(index);

		if (slot != -1) {
			player.inventory.setInventorySlotContents(slot, stack);
			markDirty();
		}

	}

	@Override
	public void markDirty() {
		player.inventory.markDirty();
		player.openContainer.detectAndSendChanges();
	}

	@Override
	public int getInventoryStackLimit() {
		return player.inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (invalid(index)) {
			return false;
		}

		int slot = getIndex(index);
		return slot != -1 && player.inventory.isItemValidForSlot(slot, stack);
	}

	public boolean invalid(int index) {
		return index >= 4 && index < 8;
	}

	public int getIndex(int index) {
		if (index == 8) {
			return 40;
		} else if (index >= 0 && index <= 3) {
			return 39 - index;
		} else if (index >= 9 && index <= 35) {
			return index;
		} else if (index >= 36 && index <= 44) {
			return index - 36;
		}

		return -1;
	}
}
