package com.unity.orders.gui;

import com.unity.orders.UnityOrders;
import com.unity.orders.managers.OrderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages creation and lifecycle of all GUI sessions.
 *
 * <p>Each open GUI is tracked as a {@link GuiSession} keyed by player UUID.
 * Sessions are cleaned up on inventory close or player disconnect.</p>
 */
public final class GuiManager {

    private final UnityOrders plugin;
    private final Map<UUID, GuiSession> sessions = new ConcurrentHashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public GuiManager(@NotNull UnityOrders plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the main marketplace GUI for a player.
     *
     * @param player the player
     */
    public void openMain(@NotNull Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize("<dark_green>UnityOrders Marketplace</dark_green>"));

        // Create order button
        inv.setItem(11, createButton(Material.WRITABLE_BOOK, "<green>Create Buy Order</green>", "Click to create a new buy order"));
        // View my orders
        inv.setItem(13, createButton(Material.CHEST, "<gold>My Orders</gold>", "View your active buy orders"));
        // Browse all orders
        inv.setItem(15, createButton(Material.COMPASS, "<aqua>Browse Orders</aqua>", "Browse all open orders"));
        // Close
        inv.setItem(49, createButton(Material.BARRIER, "<red>Close</red>", "Close the menu"));

        player.openInventory(inv);
        sessions.put(player.getUniqueId(), new GuiSession(player.getUniqueId(), GuiType.MAIN, 0));
    }

    /**
     * Opens the "My Orders" GUI for a player.
     *
     * @param player the player
     * @param page   the page number (0-indexed)
     */
    public void openMyOrders(@NotNull Player player, int page) {
        List<OrderManager.BuyOrder> orders = plugin.getOrderManager().getOrders(player.getUniqueId());
        openOrdersList(player, orders, "<gold>My Orders</gold>", GuiType.MY_ORDERS, page);
    }

    /**
     * Opens the "Browse Orders" GUI for a player.
     *
     * @param player the player
     * @param page   the page number (0-indexed)
     */
    public void openBrowse(@NotNull Player player, int page) {
        List<OrderManager.BuyOrder> orders = plugin.getOrderManager().getAllOrders();
        openOrdersList(player, orders, "<aqua>Browse Orders</aqua>", GuiType.BROWSE, page);
    }

    private void openOrdersList(@NotNull Player player, @NotNull List<OrderManager.BuyOrder> orders,
                                @NotNull String title, @NotNull GuiType type, int page) {
        int pageSize = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) orders.size() / pageSize));
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(title + " <gray>(" + (page + 1) + "/" + totalPages + ")</gray>"));

        int start = page * pageSize;
        int end = Math.min(start + pageSize, orders.size());

        for (int i = start; i < end; i++) {
            OrderManager.BuyOrder order = orders.get(i);
            int slot = i - start;
            inv.setItem(slot, createOrderItem(order));
        }

        // Navigation
        if (page > 0) {
            inv.setItem(45, createButton(Material.ARROW, "<yellow>Previous Page</yellow>", "Page " + page));
        }
        if (page < totalPages - 1) {
            inv.setItem(53, createButton(Material.ARROW, "<yellow>Next Page</yellow>", "Page " + (page + 2)));
        }
        inv.setItem(49, createButton(Material.BARRIER, "<red>Back</red>", "Return to main menu"));

        player.openInventory(inv);
        sessions.put(player.getUniqueId(), new GuiSession(player.getUniqueId(), type, page));
    }

    @NotNull
    private ItemStack createButton(@NotNull Material material, @NotNull String name, @NotNull String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(name));
            meta.lore(List.of(miniMessage.deserialize("<gray>" + lore + "</gray>")));
            item.setItemMeta(meta);
        }
        return item;
    }

    @NotNull
    private ItemStack createOrderItem(@NotNull OrderManager.BuyOrder order) {
        ItemStack item = new ItemStack(order.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<green>Order #" + order.getOrderId() + "</green>"));
            meta.lore(List.of(
                    miniMessage.deserialize("<gray>Buyer: <white>" + order.getBuyerName() + "</white></gray>"),
                    miniMessage.deserialize("<gray>Amount: <white>" + order.getRemaining() + "/" + order.getTotalAmount() + "</white></gray>"),
                    miniMessage.deserialize("<gray>Price: <yellow>" + order.getPricePerUnit() + "</yellow> each</gray>"),
                    miniMessage.deserialize("<gray>Total: <yellow>" + (order.getRemaining() * order.getPricePerUnit()) + "</yellow></gray>")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Gets the active GUI session for a player.
     *
     * @param playerUUID the player UUID
     * @return the session, or null if none
     */
    @Nullable
    public GuiSession getSession(@NotNull UUID playerUUID) {
        return sessions.get(playerUUID);
    }

    /**
     * Removes a player's GUI session.
     *
     * @param playerUUID the player UUID
     */
    public void removeSession(@NotNull UUID playerUUID) {
        sessions.remove(playerUUID);
    }

    /**
     * Checks if a player has an active GUI session.
     *
     * @param playerUUID the player UUID
     * @return true if a session exists
     */
    public boolean hasSession(@NotNull UUID playerUUID) {
        return sessions.containsKey(playerUUID);
    }
}
