package de.leonfoundU.coinflip.config;

import org.bukkit.configuration.file.FileConfiguration;

public record Settings(double minBet, double maxBet, double taxPercent, int maxOpenPerPlayer, double broadcastMinBet) {

    static Settings from(FileConfiguration config) {
        double min = Math.max(0.01, config.getDouble("min-bet", 100));
        double max = Math.max(min, config.getDouble("max-bet", 10_000_000));
        double tax = Math.clamp(config.getDouble("tax-percent", 0), 0, 100);
        int maxOpen = Math.max(1, config.getInt("max-open-per-player", 3));
        double broadcast = config.getDouble("broadcast-min-bet", 10_000);
        return new Settings(min, max, tax, maxOpen, broadcast);
    }

    public boolean shouldBroadcast(double amount) {
        return broadcastMinBet >= 0 && amount >= broadcastMinBet;
    }
}
