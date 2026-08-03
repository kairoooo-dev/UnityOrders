package com.unity.orders.hooks.papi;

import com.unity.orders.UnityOrders;
import com.unity.orders.managers.OrderManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * PlaceholderAPI expansion for UnityOrders.
 *
 * <p>Registers the following placeholders:</p>
 * <ul>
 *   <li><code>%unityorders_total%</code> &mdash; total number of orders</li>
 *   <li><code>%unityorders_myorders%</code> &mdash; number of orders for the player</li>
 *   <li><code>%unityorders_active%</code> &mdash; number of active (unfulfilled) orders</li>
 * </ul>
 */
public final class PlaceholderAPIHook extends PlaceholderExpansion {

    private final UnityOrders plugin;

    public PlaceholderAPIHook(@NotNull UnityOrders plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "unityorders";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return plugin.getPluginMeta().getAuthors().stream().findFirst().orElse("UnityOrders");
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    @Nullable
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        List<OrderManager.BuyOrder> allOrders = plugin.getOrderManager().getAllOrders();

        return switch (params.toLowerCase()) {
            case "total" -> String.valueOf(allOrders.size());
            case "myorders" -> String.valueOf(plugin.getOrderManager().getOrders(player.getUniqueId()).size());
            case "active" -> String.valueOf(allOrders.stream().filter(o -> !o.isFulfilled()).count());
            default -> null;
        };
    }
}
