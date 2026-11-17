package de.leonfoundU.coinflip.config;

import de.leonfoundU.coinflip.text.Text;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class Messages {

    private final Map<String, String> templates = new HashMap<>();
    private final String prefix;

    private Messages(String prefix, Map<String, String> templates) {
        this.prefix = prefix;
        this.templates.putAll(templates);
    }

    static Messages from(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("messages");
        Map<String, String> templates = new HashMap<>();
        String prefix = "";
        if (section != null) {
            prefix = section.getString("prefix", "");
            for (String key : section.getKeys(false)) {
                templates.put(key, section.getString(key, key));
            }
        }
        return new Messages(prefix, templates);
    }

    public Component get(String key, TagResolver... resolvers) {
        String template = templates.getOrDefault(key, key);
        return Text.parse(prefix + template, resolvers);
    }

    public void send(Audience audience, String key, TagResolver... resolvers) {
        audience.sendMessage(get(key, resolvers));
    }
}
