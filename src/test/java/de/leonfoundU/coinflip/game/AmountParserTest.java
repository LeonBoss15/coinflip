package de.leonfoundU.coinflip.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmountParserTest {

    @Test
    void parsesPlainNumbers() {
        assertEquals(500.0, AmountParser.parse("500").orElseThrow());
        assertEquals(12.34, AmountParser.parse(" 12,345 ").orElseThrow());
    }

    @Test
    void parsesSuffixes() {
        assertEquals(2_500.0, AmountParser.parse("2.5k").orElseThrow());
        assertEquals(1_000_000.0, AmountParser.parse("1M").orElseThrow());
        assertEquals(3_000_000_000.0, AmountParser.parse("3b").orElseThrow());
    }

    @Test
    void rejectsInvalidInput() {
        assertTrue(AmountParser.parse(null).isEmpty());
        assertTrue(AmountParser.parse("").isEmpty());
        assertTrue(AmountParser.parse("-5").isEmpty());
        assertTrue(AmountParser.parse("0").isEmpty());
        assertTrue(AmountParser.parse("0.001").isEmpty());
        assertTrue(AmountParser.parse("abc").isEmpty());
        assertTrue(AmountParser.parse("1e9").isEmpty());
        assertTrue(AmountParser.parse("NaN").isEmpty());
        assertTrue(AmountParser.parse("k").isEmpty());
        assertTrue(AmountParser.parse("123456789012345678901").isEmpty());
    }
}
