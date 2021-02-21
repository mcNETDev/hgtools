package de.dwdev.hgtools;

import java.util.function.Function;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.dwdev.hgtools.dimension.HGDimensions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.server.command.CommandDimensions;
import net.minecraftforge.server.command.CommandSetDimension;
import net.minecraftforge.server.permission.PermissionAPI;

public class HGCommands {
	public static final String PERM_INVSEE = "hgtools.invsee";

	//@formatter:off
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("invsee")
					.requires(src -> {
						if(src.getEntity() instanceof ServerPlayerEntity) {
							try {
								if(PermissionAPI.hasPermission(src.asPlayer(), "hgtools.invsee")) {
									return true;
								}
								if(src.asPlayer().hasPermissionLevel(3)) {
									return true;
								}
							} catch (CommandSyntaxException e) {
								e.printStackTrace();
							}
						}
						return false;
					})
					.then(Commands.argument("player", EntityArgument.player())
					.executes(context -> invsee(context.getSource().asPlayer(), EntityArgument.getPlayer(context, "player")))));
		dispatcher.register(
				Commands.literal("hgtools")
						.then(Commands.literal("invsee")
								.requires(src -> {
									if(src.getEntity() instanceof ServerPlayerEntity) {
										try {
											if(PermissionAPI.hasPermission(src.asPlayer(), "hgtools.invsee")) {
												return true;
											}
											if(src.asPlayer().hasPermissionLevel(3)) {
												return true;
											}
										} catch (CommandSyntaxException e) {
											e.printStackTrace();
										}
									}
									return false;
								})
								.then(Commands.argument("player", EntityArgument.player())
										.executes(context -> invsee(context.getSource().asPlayer(), EntityArgument.getPlayer(context, "player"))
										)
								)
						)
		);
	}
	//@formatter:on

	private static int invsee(ServerPlayerEntity player, ServerPlayerEntity playerSeenIn) {
		player.openContainer(new INamedContainerProvider() {

			@Override
			public Container createMenu(int id, PlayerInventory pinv, PlayerEntity p) {
				return new ChestContainer(ContainerType.GENERIC_9X5, id, pinv, new OPlayerInventory(playerSeenIn), 5);
			}

			@Override
			public ITextComponent getDisplayName() {
				return playerSeenIn.getDisplayName();
			}
		});

		return 1;
	}

}
