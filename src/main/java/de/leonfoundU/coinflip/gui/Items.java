package de.leonfoundU.coinflip.gui;

import de.leonfoundU.coinflip.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

final class Items {

    private Items() {
    }

    static Component text(String miniMessage, TagResolver... resolvers) {
        return Text.parse(miniMessage, resolvers).decoration(TextDecoration.ITALIC, false);
    }

    static ItemStack item(Material material, String name, String... lore) {
        return item(material, name, TagResolver.empty(), lore);
    }

    static ItemStack item(Material material, String name, TagResolver resolver, String... lore) {
        ItemStack item = ItemStack.of(material);
        item.editMeta(meta -> {
            meta.displayName(text(name, resolver));
            List<Component> lines = Arrays.stream(lore).map(line -> text(line, resolver)).toList();
            if (!lines.isEmpty()) {
                meta.lore(lines);
            }
        });
        return item;
    }
}
