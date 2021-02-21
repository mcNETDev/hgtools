package de.dwdev.hgtools;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.BossBarCommand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.CustomServerBossInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.BossInfo.Color;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

@Mod(HGTools.MODID)
public class HGTools {
	public static final String MODID = "hgtools";
	public static final String NAME = "HGTools";
	public static final String VERSION = "0.0.1";

	private static final Logger LOGGER = LogManager.getLogger();

	public HGTools() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);

		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
	}

	public void setup(final FMLCommonSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.addListener(HGTools::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(HGTools::tick);
		MinecraftForge.EVENT_BUS.addListener(this::starting);
	}

	public void starting(final FMLServerStartingEvent event) {
		PermissionAPI.registerNode(HGCommands.PERM_INVSEE, DefaultPermissionLevel.OP, "Invsee other Players Inventory");
	}

	public static void registerCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
		HGCommands.register(dispatcher);
	}

	public static void tick(PlayerTickEvent event) {
		if (!(event.phase == TickEvent.Phase.START)) {
			return;
		}
		PlayerEntity player = event.player;
		if (player.getEntityWorld().isRemote) {
			return;
		}
		ServerPlayerEntity p = (ServerPlayerEntity) player;

		World w = player.getEntityWorld();
		if (player.getEntityWorld().getGameTime() % 20 == 0) {
			CustomServerBossInfo i = getBosbar(player);
			if (w.getDimensionKey().getLocation().getPath().equals("oldworld")) {
				if (!i.getPlayers().contains(p)) {
					i.addPlayer(p);
				}
			} else {
				if (i.getPlayers().contains(p)) {
					i.removePlayer(p);
				}
			}
		}
	}

	public static CustomServerBossInfo getBosbar(PlayerEntity player) {
		for (CustomServerBossInfo info : player.getServer().getCustomBossEvents().getBossbars()) {
			if (info.getId().getPath().equals("oldworld")) {
				return info;
			}
		}
		CustomServerBossInfo info = player.getServer().getCustomBossEvents().add(new ResourceLocation(MODID, "oldworld"), new StringTextComponent("This world will be deleted on 03/15/2021, so Transfer now!"));
		info.setMax(100);
		info.setValue(100);
		info.setColor(Color.RED);
		info.setVisible(true);
		return info;
	}
}
