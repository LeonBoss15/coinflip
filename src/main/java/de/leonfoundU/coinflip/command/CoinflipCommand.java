package de.leonfoundU.coinflip.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.leonfoundU.coinflip.config.PluginConfig;
import de.leonfoundU.coinflip.gui.Menus;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public final class CoinflipCommand {

    public static final String USE_PERMISSION = "coinflip.use";
    public static final String ADMIN_PERMISSION = "coinflip.admin";

    private final Menus menus;
    private final PluginConfig config;

    public CoinflipCommand(Menus menus, PluginConfig config) {
        this.menus = menus;
        this.config = config;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("coinflip")
                .requires(source -> source.getSender().hasPermission(USE_PERMISSION))
                .executes(context -> {
                    if (context.getSource().getExecutor() instanceof Player player) {
                        menus.openList(player, 0);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                        .executes(context -> {
                            config.reload();
                            config.messages().send(context.getSource().getSender(), "reloaded");
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
}
