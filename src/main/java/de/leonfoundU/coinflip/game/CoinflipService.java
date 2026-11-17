package de.leonfoundU.coinflip.game;

import de.leonfoundU.coinflip.config.Messages;
import de.leonfoundU.coinflip.config.PluginConfig;
import de.leonfoundU.coinflip.config.Settings;
import de.leonfoundU.coinflip.economy.EconomyService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public final class CoinflipService {

    private final Map<UUID, CoinflipGame> openGames = new LinkedHashMap<>();
    private final EconomyService economy;
    private final PluginConfig config;
    private final Logger logger;

    public CoinflipService(EconomyService economy, PluginConfig config, Logger logger) {
        this.economy = economy;
        this.config = config;
        this.logger = logger;
    }

    public List<CoinflipGame> openGamesNewestFirst() {
        List<CoinflipGame> games = new ArrayList<>(openGames.values());
        Collections.reverse(games);
        return games;
    }

    public boolean isOwnedBy(UUID gameId, UUID playerId) {
        CoinflipGame game = openGames.get(gameId);
        return game != null && game.creatorId().equals(playerId);
    }

    public double payoutFor(double amount) {
        BigDecimal pot = BigDecimal.valueOf(amount).multiply(BigDecimal.TWO);
        BigDecimal keep = BigDecimal.ONE.subtract(BigDecimal.valueOf(config.settings().taxPercent()).movePointLeft(2));
        return pot.multiply(keep).setScale(2, RoundingMode.DOWN).doubleValue();
    }

    public boolean validateAmount(Player player, double amount) {
        Settings settings = config.settings();
        if (amount < settings.minBet() || amount > settings.maxBet()) {
            messages().send(player, "amount-out-of-range",
                    Placeholder.unparsed("min", economy.format(settings.minBet())),
                    Placeholder.unparsed("max", economy.format(settings.maxBet())));
            return false;
        }
        return true;
    }

    public boolean create(Player player, double amount) {
        if (!validateAmount(player, amount)) {
            return false;
        }
        int maxOpen = config.settings().maxOpenPerPlayer();
        long ownOpen = openGames.values().stream().filter(game -> game.creatorId().equals(player.getUniqueId())).count();
        if (ownOpen >= maxOpen) {
            messages().send(player, "too-many-open", Placeholder.unparsed("max", String.valueOf(maxOpen)));
            return false;
        }
        if (!withdraw(player, amount)) {
            return false;
        }

        CoinflipGame game = new CoinflipGame(UUID.randomUUID(), player.getUniqueId(), player.getName(), amount);
        openGames.put(game.id(), game);
        messages().send(player, "created", amountTag(amount));
        return true;
    }

    public void cancel(Player player, UUID gameId) {
        CoinflipGame game = openGames.get(gameId);
        if (game == null || !game.creatorId().equals(player.getUniqueId())) {
            messages().send(player, "not-available");
            return;
        }
        openGames.remove(gameId);
        refund(game);
        messages().send(player, "cancelled", amountTag(game.amount()));
    }

    public void join(Player joiner, UUID gameId) {
        CoinflipGame game = openGames.get(gameId);
        if (game == null || game.creatorId().equals(joiner.getUniqueId())) {
            messages().send(joiner, "not-available");
            return;
        }
        if (!withdraw(joiner, game.amount())) {
            return;
        }
        openGames.remove(gameId);

        OfflinePlayer creator = Bukkit.getOfflinePlayer(game.creatorId());
        boolean creatorWins = ThreadLocalRandom.current().nextBoolean();
        OfflinePlayer winner = creatorWins ? creator : joiner;
        String winnerName = creatorWins ? game.creatorName() : joiner.getName();
        String loserName = creatorWins ? joiner.getName() : game.creatorName();
        double payout = payoutFor(game.amount());

        EconomyResponse response = economy.deposit(winner, payout);
        if (!response.transactionSuccess()) {
            logger.severe("Coinflip payout of " + payout + " to " + winner.getUniqueId() + " failed: " + response.errorMessage);
        }

        notifyResult(winner, loserName, game.amount(), payout, true);
        notifyResult(creatorWins ? joiner : creator, winnerName, game.amount(), payout, false);

        if (config.settings().shouldBroadcast(game.amount())) {
            Bukkit.getServer().sendMessage(messages().get("broadcast",
                    Placeholder.unparsed("winner", winnerName),
                    Placeholder.unparsed("loser", loserName),
                    amountTag(game.amount())));
        }
    }

    public void refundAll() {
        for (CoinflipGame game : openGames.values()) {
            refund(game);
            Player creator = Bukkit.getPlayer(game.creatorId());
            if (creator != null) {
                messages().send(creator, "refunded-shutdown", amountTag(game.amount()));
            }
        }
        openGames.clear();
    }

    private void notifyResult(OfflinePlayer target, String opponentName, double amount, double payout, boolean won) {
        Player online = target.getPlayer();
        if (online == null) {
            return;
        }
        messages().send(online, won ? "won" : "lost",
                Placeholder.unparsed("opponent", opponentName),
                Placeholder.unparsed("payout", economy.format(payout)),
                amountTag(amount));
    }

    private boolean withdraw(Player player, double amount) {
        if (!economy.has(player, amount)) {
            messages().send(player, "not-enough-money", amountTag(amount));
            return false;
        }
        EconomyResponse response = economy.withdraw(player, amount);
        if (!response.transactionSuccess()) {
            messages().send(player, "transaction-failed", Placeholder.unparsed("error", String.valueOf(response.errorMessage)));
            return false;
        }
        return true;
    }

    private void refund(CoinflipGame game) {
        OfflinePlayer creator = Bukkit.getOfflinePlayer(game.creatorId());
        EconomyResponse response = economy.deposit(creator, game.amount());
        if (!response.transactionSuccess()) {
            logger.severe("Coinflip refund of " + game.amount() + " to " + game.creatorId() + " failed: " + response.errorMessage);
        }
    }

    private TagResolver amountTag(double amount) {
        return Placeholder.unparsed("amount", economy.format(amount));
    }

    private Messages messages() {
        return config.messages();
    }
}
