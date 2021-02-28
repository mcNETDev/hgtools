package de.dwdev.hgtools;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;

public class HGApi extends WorldSavedData {
    private static HGApi instance;
    public boolean editMode = false;
    private ArrayList<ResourceLocation> bannedItems = new ArrayList<>();


    public HGApi() {
        super(HGTools.MODID);
        initStacks();
    }

    public static HGApi get(ServerWorld world) {
        return world.getSavedData().getOrCreate(HGApi::new, HGTools.MODID);
    }

    public static HGApi get(MinecraftServer server) {
        return server.getWorld(World.OVERWORLD).getSavedData().getOrCreate(HGApi::new, HGTools.MODID);
    }

    public void initStacks() {
        bannedItems = new ArrayList<>();
    }

    @Override
    public void read(CompoundNBT nbt) {
        if (nbt.contains("bannedItems")) {
            initStacks();
            ListNBT list = nbt.getList("bannedItems", nbt.getId());
            list.forEach(t -> {
                CompoundNBT i = (CompoundNBT) t;
                bannedItems.add(new ResourceLocation(i.getString("namespace"), i.getString("path")));
            });
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        bannedItems.forEach(stack -> {
            if (stack != null) {
                CompoundNBT tag = new CompoundNBT();
                tag.putString("namespace", stack.getNamespace());
                tag.putString("path", stack.getPath());
                list.add(tag);
            }
        });
        compound.put("bannedItems", list);
        return compound;
    }


    public boolean isItemBanned(ItemStack itemStack) {
        ItemStack a = itemStack.copy();
        if (editMode) {
            return false;
        }
        for (ResourceLocation bannedItem : bannedItems) {
            if (a.getItem().getRegistryName().equals(bannedItem)) {
                return true;
            }
        }
        return false;
    }


    public ArrayList<ResourceLocation> getBannedItems() {
        return bannedItems;
    }

    public void removeBannedItem(ItemStack heldItemMainhand) {
        for (int i = 0; i < bannedItems.size(); i++) {
            if(bannedItems.get(i).equals(heldItemMainhand.getItem().getRegistryName())){
                bannedItems.remove(i);
            }
        }
    }

    public void addBannedItem(ItemStack stack) {
        bannedItems.add(stack.getItem().getRegistryName());
        markDirty();
    }

    public void checkPlayerInv(ServerPlayerEntity player) {
        if(editMode){
            return;
        }
        if(player.openContainer != null){
            for (int i = 0; i < player.openContainer.getInventory().size(); i++) {
                ItemStack s = player.openContainer.getInventory().get(i);
                if(s != null && s.getItem() != null){
                    if(isItemBanned(s)){
                        s.setCount(0);
                        player.openContainer.getInventory().set(i, ItemStack.EMPTY);
                    }
                }
            }
        }
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack s = player.inventory.mainInventory.get(i);
            if(s != null && s.getItem() != null){
                if(isItemBanned(s)){
                    s.setCount(0);
                    player.inventory.mainInventory.set(i, ItemStack.EMPTY);
                }
            }
        }
        for (int i = 0; i < player.inventory.armorInventory.size(); i++) {
            ItemStack s = player.inventory.armorInventory.get(i);
            if(s != null && s.getItem() != null){
                if(isItemBanned(s)){
                    s.setCount(0);
                    player.inventory.armorInventory.set(i, ItemStack.EMPTY);
                }
            }
        }
        for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
            ItemStack s = player.inventory.offHandInventory.get(i);
            if(s != null && s.getItem() != null){
                if(isItemBanned(s)){
                    s.setCount(0);
                    player.inventory.offHandInventory.set(i, ItemStack.EMPTY);
                }
            }
        }
    }
}
