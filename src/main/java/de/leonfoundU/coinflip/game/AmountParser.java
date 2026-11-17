package de.leonfoundU.coinflip.game;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.OptionalDouble;

public final class AmountParser {

    private static final int MAX_INPUT_LENGTH = 20;

    private AmountParser() {
    }

    public static OptionalDouble parse(String raw) {
        if (raw == null) {
            return OptionalDouble.empty();
        }
        String input = raw.trim().toLowerCase(Locale.ROOT).replace(',', '.').replace("_", "");
        if (input.isEmpty() || input.length() > MAX_INPUT_LENGTH) {
            return OptionalDouble.empty();
        }

        BigDecimal multiplier = BigDecimal.ONE;
        char suffix = input.charAt(input.length() - 1);
        switch (suffix) {
            case 'k' -> multiplier = BigDecimal.valueOf(1_000L);
            case 'm' -> multiplier = BigDecimal.valueOf(1_000_000L);
            case 'b' -> multiplier = BigDecimal.valueOf(1_000_000_000L);
            default -> {
            }
        }
        if (multiplier.compareTo(BigDecimal.ONE) != 0) {
            input = input.substring(0, input.length() - 1);
        }
        if (!input.matches("[0-9]+([.][0-9]+)?")) {
            return OptionalDouble.empty();
        }

        BigDecimal value = new BigDecimal(input).multiply(multiplier).setScale(2, RoundingMode.DOWN);
        if (value.signum() <= 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(value.doubleValue());
    }
}
