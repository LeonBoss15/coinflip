package de.leonfoundU.coinflip.game;

import java.util.UUID;

public record CoinflipGame(UUID id, UUID creatorId, String creatorName, double amount) {
}
