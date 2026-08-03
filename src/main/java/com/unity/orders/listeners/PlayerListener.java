package com.unity.orders.listeners;

import com.unity.orders.UnityOrders;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for player join/quit events.
 *
 * <p>On join: sends a welcome message and checks for update notifications
 * (if the player is an admin). On quit: cleans up any active GUI sessions.</p>
 */
public final class PlayerListener implements Listener {

    private final UnityOrders plugin;

    public PlayerListener(@NotNull UnityOrders plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Clean up stale GUI sessions
        plugin.getGuiManager().removeSession(player.getUniqueId());

        // Notify admins of updates
        if (player.hasPermission("unityorders.admin") && plugin.getUpdateChecker().isUpdateAvailable()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§a[UnityOrders] §eAn update is available: v" +
                        plugin.getUpdateChecker().getLatestVersion());
                player.sendMessage("§7Download: " + plugin.getUpdateChecker().getDownloadUrl());
            }, 40L); // 2 seconds delay
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getGuiManager().removeSession(player.getUniqueId());
    }
}
