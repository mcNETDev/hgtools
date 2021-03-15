package de.dwdev.hgtools;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.CustomServerBossInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = HGTools.MODID)
public class HGEvents {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        HGCommands.register(dispatcher);
    }

    @SubscribeEvent
    public static void spawn(EntityJoinWorldEvent event) {
        World w = (World) event.getWorld();

        if (w.getDimensionKey().getLocation().getPath().equals("oldworld")) {
            if ((event.getEntity() instanceof PlayerEntity) || (event.getEntity() instanceof ItemEntity)) {

            } else {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (!(event.phase == TickEvent.Phase.START)) {
            return;
        }
        PlayerEntity player = event.player;
        if (player.getEntityWorld().isRemote) {
            return;
        }
        ServerPlayerEntity p = (ServerPlayerEntity) player;

        World w = player.getEntityWorld();
//        if (player.getEntityWorld().getGameTime() % 20 == 0) {
//            CustomServerBossInfo i = getBosbar(player);
//            if (w.getDimensionKey().getLocation().getPath().equals("oldworld")) {
//                if (!i.getPlayers().contains(p)) {
//                    i.addPlayer(p);
//                }
//            } else {
//                if (i.getPlayers().contains(p)) {
//                    i.removePlayer(p);
//                }
//            }
//
//            if (w.getDimensionKey().getLocation().getPath().equals("overworld")) {
//
//                CustomServerBossInfo b2 = getBosbarLeave(player);
//                BlockPos pos = player.getPosition();
//                if (pos.getX() > 10000 || pos.getZ() > 10000 || pos.getX() < -10000 || pos.getZ() < -10000) {
//                    if (!b2.getPlayers().contains(p)) {
//                        b2.addPlayer(p);
//                    }
//                } else {
//                    if (b2.getPlayers().contains(p)) {
//                        b2.removePlayer(p);
//                    }
//                }
//            }
//        }
        if (player.getEntityWorld().getGameTime() % 10 == 0) {
            HGApi.get(player.getServer()).checkPlayerInv(p);
        }
    }

    public static CustomServerBossInfo getBosbarLeave(PlayerEntity player) {
        for (CustomServerBossInfo info : player.getServer().getCustomBossEvents().getBossbars()) {
            if (info.getId().getPath().equals("leave")) {
                return info;
            }
        }
        CustomServerBossInfo info = player.getServer().getCustomBossEvents().add(new ResourceLocation(HGTools.MODID, "leave"), new StringTextComponent("DO NOT BUILD HERE! Your stuff will be lost forever after worldborder is set!"));

        info.setMax(100);
        info.setValue(100);
        info.setColor(BossInfo.Color.RED);
        info.setVisible(true);
        return info;
    }

    public static CustomServerBossInfo getBosbar(PlayerEntity player) {
        for (CustomServerBossInfo info : player.getServer().getCustomBossEvents().getBossbars()) {
            if (info.getId().getPath().equals("oldworld")) {
                return info;
            }
        }

        CustomServerBossInfo info = player.getServer().getCustomBossEvents().add(new ResourceLocation(HGTools.MODID, "oldworld"), new StringTextComponent("This world will be deleted on 03/15/2021, so Transfer now!"));
        info.setMax(100);
        info.setValue(100);
        info.setColor(BossInfo.Color.RED);
        info.setVisible(true);
        return info;
    }

    @SubscribeEvent
    public void starting(final FMLServerStartingEvent event) {

    }
}
