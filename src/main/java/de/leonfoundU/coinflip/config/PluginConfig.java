package de.leonfoundU.coinflip.config;

import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConfig {

    private final JavaPlugin plugin;
    private volatile Settings settings;
    private volatile Messages messages;

    public PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        load();
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    private void load() {
        settings = Settings.from(plugin.getConfig());
        messages = Messages.from(plugin.getConfig());
    }

    public Settings settings() {
        return settings;
    }

    public Messages messages() {
        return messages;
    }
}
