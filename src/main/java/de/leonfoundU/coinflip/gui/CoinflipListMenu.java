package de.leonfoundU.coinflip.gui;

import de.leonfoundU.coinflip.game.CoinflipGame;
import de.leonfoundU.coinflip.game.CoinflipService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class CoinflipListMenu implements Menu {

    private static final int SIZE = 36;
    private static final int GAMES_PER_PAGE = 27;
    private static final int RELOAD_SLOT = 27;
    private static final int PREVIOUS_SLOT = 30;
    private static final int INFO_SLOT = 31;
    private static final int NEXT_SLOT = 32;
    private static final int CREATE_SLOT = 35;

    private final Menus menus;
    private final Inventory inventory;
    private final Map<Integer, UUID> gamesBySlot = new HashMap<>();
    private final int page;
    private final int pageCount;

    CoinflipListMenu(Menus menus, int requestedPage) {
        this.menus = menus;
        this.inventory = Bukkit.createInventory(this, SIZE, Items.text("<dark_gray>Coinflips"));

        List<CoinflipGame> games = menus.coinflips().openGamesNewestFirst();
        this.pageCount = Math.max(1, (games.size() + GAMES_PER_PAGE - 1) / GAMES_PER_PAGE);
        this.page = Math.clamp(requestedPage, 0, pageCount - 1);

        int from = page * GAMES_PER_PAGE;
        int to = Math.min(games.size(), from + GAMES_PER_PAGE);
        for (int index = from; index < to; index++) {
            CoinflipGame game = games.get(index);
            int slot = index - from;
            gamesBySlot.put(slot, game.id());
            inventory.setItem(slot, gameItem(game));
        }

        inventory.setItem(RELOAD_SLOT, Items.item(Material.ANVIL, "<yellow>Neu laden",
                "<gray>Zeigt neu erstellte Coinflips an"));
        inventory.setItem(PREVIOUS_SLOT, Items.item(Material.RED_STAINED_GLASS_PANE, "<red>Vorherige Seite"));
        inventory.setItem(INFO_SLOT, Items.item(Material.CHEST_MINECART, "<gold>Seite <page>/<pages>",
                TagResolver.resolver(
                        Placeholder.unparsed("page", String.valueOf(page + 1)),
                        Placeholder.unparsed("pages", String.valueOf(pageCount)),
                        Placeholder.unparsed("count", String.valueOf(games.size()))),
                "<gray>Offene Coinflips: <white><count>"));
        inventory.setItem(NEXT_SLOT, Items.item(Material.LIME_STAINED_GLASS_PANE, "<green>Nächste Seite"));
        inventory.setItem(CREATE_SLOT, Items.item(Material.LIME_DYE, "<green>Coinflip erstellen",
                "<gray>Erstelle deinen eigenen Coinflip"));
    }

    private ItemStack gameItem(CoinflipGame game) {
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.unparsed("player", game.creatorName()),
                Placeholder.unparsed("amount", menus.economy().format(game.amount())),
                Placeholder.unparsed("payout", menus.economy().format(menus.coinflips().payoutFor(game.amount()))));
        ItemStack head = Items.item(Material.PLAYER_HEAD, "<yellow><player>", placeholders,
                "<gray>Einsatz: <gold><amount>",
                "<gray>Gewinn: <green><payout>",
                "",
                "<yellow>Klicke zum Beitreten",
                "<dark_gray>Eigener Coinflip: Klick zum Abbrechen");
        head.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(Bukkit.getOfflinePlayer(game.creatorId())));
        return head;
    }

    @Override
    public void handleClick(Player player, int slot) {
        switch (slot) {
            case RELOAD_SLOT -> menus.openList(player, page);
            case PREVIOUS_SLOT -> {
                if (page > 0) {
                    menus.openList(player, page - 1);
                }
            }
            case NEXT_SLOT -> {
                if (page + 1 < pageCount) {
                    menus.openList(player, page + 1);
                }
            }
            case CREATE_SLOT -> menus.openCreate(player, null);
            default -> handleGameClick(player, slot);
        }
    }

    private void handleGameClick(Player player, int slot) {
        UUID gameId = gamesBySlot.get(slot);
        if (gameId == null) {
            return;
        }
        CoinflipService coinflips = menus.coinflips();
        if (coinflips.isOwnedBy(gameId, player.getUniqueId())) {
            coinflips.cancel(player, gameId);
        } else {
            coinflips.join(player, gameId);
        }
        menus.openList(player, page);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
