package de.leonfoundU.coinflip.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public interface Menu extends InventoryHolder {

    void handleClick(Player player, int slot);
}
