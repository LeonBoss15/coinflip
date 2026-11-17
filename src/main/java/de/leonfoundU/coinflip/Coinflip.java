package de.leonfoundU.coinflip;

import de.leonfoundU.coinflip.command.CoinflipCommand;
import de.leonfoundU.coinflip.config.PluginConfig;
import de.leonfoundU.coinflip.economy.EconomyService;
import de.leonfoundU.coinflip.game.CoinflipService;
import de.leonfoundU.coinflip.gui.MenuListener;
import de.leonfoundU.coinflip.gui.Menus;
import de.leonfoundU.coinflip.input.SignInputService;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Coinflip extends JavaPlugin {

    private CoinflipService coinflips;
    private SignInputService signInput;
    private Menus menus;

    @Override
    public void onEnable() {
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            getLogger().severe("Kein Vault-Economy-Provider gefunden (z.B. EssentialsX). Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginConfig config = new PluginConfig(this);
        EconomyService economy = new EconomyService(provider.getProvider());
        coinflips = new CoinflipService(economy, config, getLogger());
        signInput = new SignInputService(this);
        menus = new Menus(this, coinflips, signInput, economy, config);

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(signInput, this);

        CoinflipCommand command = new CoinflipCommand(menus, config);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(command.build(), "Öffnet das Coinflip-Menü", List.of("cf")));
    }

    @Override
    public void onDisable() {
        if (menus != null) {
            menus.closeAll();
        }
        if (signInput != null) {
            signInput.cancelAll();
        }
        if (coinflips != null) {
            coinflips.refundAll();
        }
    }
}
