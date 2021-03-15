package de.dwdev.hgtools;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.dwdev.hgtools.inventory.OPlayerInventory;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.UUID;

public class HGCommands {
    public static final UUID sender = UUID.randomUUID();


    //@formatter:off
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("invsee")
                        .requires(src -> {
                            return hasPermission(src);
                        })
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> invsee(context.getSource().asPlayer(), EntityArgument.getPlayer(context, "player")))));
        dispatcher.register(
                Commands.literal("hgtools")
                        .then(Commands.literal("invsee")
                                .requires(src -> {
                                    return hasPermission(src);
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> invsee(context.getSource().asPlayer(), EntityArgument.getPlayer(context, "player"))
                                        )
                                )
                        )
                        .then(Commands.literal("ban_item")
                                .requires(src -> {
                                    return hasPermission(src);
                                })
                                .then(Commands.literal("list")
                                        .executes(context -> listBannedItems(context)
                                        )
                                )
                                .then(Commands.literal("editmode")
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(context -> toggleEditmode(context, BoolArgumentType.getBool(context, "enabled"))
                                                )
                                        )
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.literal("hand")
                                                .executes(context -> banItem(context, context.getSource().asPlayer().getHeldItemMainhand())
                                                )
                                        )
                                        .then(Commands.argument("item", ItemArgument.item())
                                                .executes(context -> banItem(context, ItemArgument.getItem(context, "item").createStack(1, false)
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.literal("hand")
                                                .executes(context -> unbanItem(context, context.getSource().asPlayer().getHeldItemMainhand())
                                                )
                                        )
                                        .then(Commands.argument("item", ItemArgument.item())
                                                .executes(context -> unbanItem(context, ItemArgument.getItem(context, "item").createStack(1, false))
                                                )
                                        )
                                )
                        )
        );
    }  //@formatter:on

    private static int unbanItem(CommandContext<CommandSource> context, ItemStack itemstack) throws CommandSyntaxException {
        ServerPlayerEntity p = context.getSource().asPlayer();
        if (HGApi.get(p.getServer()).containsItem(itemstack)) {
            HGApi.get(p.getServer()).removeBannedItem(itemstack);
            p.sendMessage(new StringTextComponent(TextFormatting.GRAY + "Removed " + itemstack.getItem().getRegistryName() + " from the ban list!"), sender);
        } else {
            p.sendMessage(new StringTextComponent(TextFormatting.GRAY + "Item is not on the Ban list!"), sender);
        }
        return 1;
    }

    private static int banItem(CommandContext<CommandSource> context, ItemStack heldItemMainhand) throws CommandSyntaxException {
        ServerPlayerEntity p = context.getSource().asPlayer();
        if (HGApi.get(p.getServer()).containsItem(heldItemMainhand)) {
            p.sendMessage(new StringTextComponent(TextFormatting.GRAY + "Item is already banned!"), sender);
        } else {
            HGApi.get(p.getServer()).addBannedItem(heldItemMainhand);
            p.sendMessage(new StringTextComponent(TextFormatting.GRAY + "Added " + heldItemMainhand.getItem().getRegistryName() + " to the ban list!"), sender);
        }
        return 1;
    }

    private static int toggleEditmode(CommandContext<CommandSource> context, boolean enabled) throws CommandSyntaxException {
        HGApi.get(context.getSource().getServer()).editMode = enabled;
        context.getSource().asPlayer().sendMessage(new StringTextComponent(TextFormatting.GRAY + "Global Editmode is now " + (enabled ? "on" : "off")), sender);
        return 1;
    }

    private static int listBannedItems(CommandContext<CommandSource> context) throws CommandSyntaxException {
        HGApi.get(context.getSource().getServer()).getBannedItems().forEach(itemStack -> {
            try {
                if (itemStack != null) {
                    context.getSource().asPlayer().sendMessage(new StringTextComponent(TextFormatting.GRAY + itemStack.toString()), sender);
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        });

        return 1;
    }

    private static boolean hasPermission(CommandSource src) {
        if (src.getEntity() instanceof ServerPlayerEntity) {
            try {
                if (src.asPlayer().hasPermissionLevel(3)) {
                    return true;
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

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
