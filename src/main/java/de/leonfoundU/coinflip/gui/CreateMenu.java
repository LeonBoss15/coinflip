package de.leonfoundU.coinflip.gui;

import de.leonfoundU.coinflip.game.AmountParser;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.OptionalDouble;

final class CreateMenu implements Menu {

    private static final int SIZE = 27;
    private static final int AMOUNT_SLOT = 13;
    private static final int INPUT_SLOT = 22;
    private static final int CONFIRM_SLOT = 26;

    private final Menus menus;
    private final Inventory inventory;
    private final Double amount;

    CreateMenu(Menus menus, Double amount) {
        this.menus = menus;
        this.amount = amount;
        this.inventory = Bukkit.createInventory(this, SIZE, Items.text("<dark_gray>Coinflip erstellen"));

        String amountText = amount == null ? "Nicht festgelegt" : menus.economy().format(amount);
        inventory.setItem(AMOUNT_SLOT, Items.item(Material.OAK_SIGN, "<gold>Betrag: <yellow><amount>",
                Placeholder.unparsed("amount", amountText)));
        inventory.setItem(INPUT_SLOT, Items.item(Material.ANVIL, "<yellow>Betrag eingeben",
                "<gray>Klicke, um den Einsatz", "<gray>auf einem Schild einzugeben"));
        inventory.setItem(CONFIRM_SLOT, Items.item(Material.LIME_STAINED_GLASS_PANE, "<green>Bestätigen",
                "<gray>Erstellt den Coinflip"));
    }

    @Override
    public void handleClick(Player player, int slot) {
        switch (slot) {
            case INPUT_SLOT -> requestAmount(player);
            case CONFIRM_SLOT -> confirm(player);
            default -> {
            }
        }
    }

    private void requestAmount(Player player) {
        menus.runNextTick(player, () -> {
            player.closeInventory();
            menus.signInput().open(player, List.of(
                    Items.text("^^^^^^^^^^^^"),
                    Items.text("Betrag eingeben"),
                    Items.text("z.B. 500, 2.5k, 1m")
            ), input -> handleInput(player, input));
        });
    }

    private void handleInput(Player player, String input) {
        OptionalDouble parsed = AmountParser.parse(input);
        if (parsed.isEmpty()) {
            if (!input.isBlank()) {
                menus.config().messages().send(player, "invalid-amount", Placeholder.unparsed("input", input));
            }
            menus.openCreate(player, amount);
            return;
        }
        double value = parsed.getAsDouble();
        menus.openCreate(player, menus.coinflips().validateAmount(player, value) ? value : amount);
    }

    private void confirm(Player player) {
        if (amount == null) {
            menus.config().messages().send(player, "no-amount");
            return;
        }
        if (menus.coinflips().create(player, amount)) {
            menus.openList(player, 0);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
