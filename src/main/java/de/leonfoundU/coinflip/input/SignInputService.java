package de.leonfoundU.coinflip.input;

import io.papermc.paper.event.packet.UncheckedSignChangeEvent;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class SignInputService implements Listener {

    private static final int BLOCKS_BELOW_PLAYER = 3;
    private static final int MAX_LINES = 4;

    private final Plugin plugin;
    private final Map<UUID, PendingInput> pending = new ConcurrentHashMap<>();

    public SignInputService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, List<Component> hintLines, Consumer<String> callback) {
        cancel(player);

        Location location = player.getLocation().toBlockLocation();
        location.setY(Math.max(player.getWorld().getMinHeight(), location.getBlockY() - BLOCKS_BELOW_PLAYER));

        BlockData signData = Material.OAK_SIGN.createBlockData();
        Sign sign = (Sign) signData.createBlockState();
        SignSide front = sign.getSide(Side.FRONT);
        for (int line = 1; line < MAX_LINES && line - 1 < hintLines.size(); line++) {
            front.line(line, hintLines.get(line - 1));
        }

        player.sendBlockChange(location, signData);
        player.sendBlockUpdate(location, sign);
        player.openVirtualSign(Position.block(location), Side.FRONT);
        pending.put(player.getUniqueId(), new PendingInput(location, callback));
    }

    public void cancel(Player player) {
        PendingInput input = pending.remove(player.getUniqueId());
        if (input != null) {
            restoreBlock(player, input.location());
        }
    }

    public void cancelAll() {
        for (UUID playerId : pending.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                cancel(player);
            }
        }
        pending.clear();
    }

    @EventHandler
    public void onSignInput(UncheckedSignChangeEvent event) {
        Player player = event.getPlayer();
        PendingInput input = pending.get(player.getUniqueId());
        if (input == null || !matches(event.getEditedBlockPosition(), input.location())) {
            return;
        }
        pending.remove(player.getUniqueId());
        event.setCancelled(true);

        String text = PlainTextComponentSerializer.plainText().serialize(event.lines().getFirst());
        Runnable handle = () -> {
            if (!player.isOnline()) {
                return;
            }
            restoreBlock(player, input.location());
            input.callback().accept(text);
        };
        if (Bukkit.isPrimaryThread()) {
            handle.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, handle);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }

    private static boolean matches(BlockPosition position, Location location) {
        return position.blockX() == location.getBlockX()
                && position.blockY() == location.getBlockY()
                && position.blockZ() == location.getBlockZ();
    }

    private static void restoreBlock(Player player, Location location) {
        if (player.getWorld().equals(location.getWorld())) {
            player.sendBlockChange(location, location.getBlock().getBlockData());
        }
    }

    private record PendingInput(Location location, Consumer<String> callback) {
    }
}
