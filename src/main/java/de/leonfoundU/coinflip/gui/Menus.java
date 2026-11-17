package de.leonfoundU.coinflip.gui;

import de.leonfoundU.coinflip.config.PluginConfig;
import de.leonfoundU.coinflip.economy.EconomyService;
import de.leonfoundU.coinflip.game.CoinflipService;
import de.leonfoundU.coinflip.input.SignInputService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class Menus {

    private final Plugin plugin;
    private final CoinflipService coinflips;
    private final SignInputService signInput;
    private final EconomyService economy;
    private final PluginConfig config;

    public Menus(Plugin plugin, CoinflipService coinflips, SignInputService signInput, EconomyService economy, PluginConfig config) {
        this.plugin = plugin;
        this.coinflips = coinflips;
        this.signInput = signInput;
        this.economy = economy;
        this.config = config;
    }

    public void openList(Player player, int page) {
        runNextTick(player, () -> player.openInventory(new CoinflipListMenu(this, page).getInventory()));
    }

    public void openCreate(Player player, Double amount) {
        runNextTick(player, () -> player.openInventory(new CreateMenu(this, amount).getInventory()));
    }

    public void closeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof Menu) {
                player.closeInventory();
            }
        }
    }

    void runNextTick(Player player, Runnable action) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                action.run();
            }
        });
    }

    CoinflipService coinflips() {
        return coinflips;
    }

    SignInputService signInput() {
        return signInput;
    }

    EconomyService economy() {
        return economy;
    }

    PluginConfig config() {
        return config;
    }
}
