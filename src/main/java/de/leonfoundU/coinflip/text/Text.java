package de.leonfoundU.coinflip.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public final class Text {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final String SHADOW = "<shadow:#000000FF>";

    private Text() {
    }

    public static Component parse(String miniMessage, TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(SHADOW + miniMessage, resolvers);
    }
}
