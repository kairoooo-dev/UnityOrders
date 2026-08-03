package com.unity.orders.listeners;

import com.unity.orders.UnityOrders;
import com.unity.orders.gui.GuiSession;
import com.unity.orders.gui.GuiType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for inventory interactions and routes them to the appropriate
 * GUI handler based on the player's active {@link GuiSession}.
 *
 * <p>Click events are cancelled to prevent item theft from GUIs.
 * Shift-click is blocked entirely in managed GUIs.</p>
 */
public final class GuiListener implements Listener {

    private final UnityOrders plugin;

    public GuiListener(@NotNull UnityOrders plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        GuiSession session = plugin.getGuiManager().getSession(player.getUniqueId());
        if (session == null) {
            return;
        }

        // Cancel all clicks in managed GUIs
        event.setCancelled(true);

        // Block shift-click
        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            return;
        }

        int slot = event.getRawSlot();

        switch (session.getGuiType()) {
            case MAIN -> handleMainClick(player, slot);
            case MY_ORDERS -> handleMyOrdersClick(player, slot, session);
            case BROWSE -> handleBrowseClick(player, slot, session);
            case CREATE_ORDER, ORDER_DETAIL -> {
                // Future: handle create-order input
            }
        }
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        // Delay session removal by 1 tick to allow for GUI switching
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!plugin.getGuiManager().hasSession(player.getUniqueId())) {
                return;
            }
            // Check if player opened a new inventory
            if (player.getOpenInventory().getTopInventory().getLocation() == null) {
                plugin.getGuiManager().removeSession(player.getUniqueId());
            }
        });
    }

    private void handleMainClick(@NotNull Player player, int slot) {
        switch (slot) {
            case 11 -> plugin.getGuiManager().openMain(player); // Create order (future)
            case 13 -> plugin.getGuiManager().openMyOrders(player, 0);
            case 15 -> plugin.getGuiManager().openBrowse(player, 0);
            case 49 -> player.closeInventory();
            default -> { }
        }
    }

    private void handleMyOrdersClick(@NotNull Player player, int slot, @NotNull GuiSession session) {
        if (slot == 45 && session.getPage() > 0) {
            plugin.getGuiManager().openMyOrders(player, session.getPage() - 1);
        } else if (slot == 53) {
            plugin.getGuiManager().openMyOrders(player, session.getPage() + 1);
        } else if (slot == 49) {
            plugin.getGuiManager().openMain(player);
        }
    }

    private void handleBrowseClick(@NotNull Player player, int slot, @NotNull GuiSession session) {
        if (slot == 45 && session.getPage() > 0) {
            plugin.getGuiManager().openBrowse(player, session.getPage() - 1);
        } else if (slot == 53) {
            plugin.getGuiManager().openBrowse(player, session.getPage() + 1);
        } else if (slot == 49) {
            plugin.getGuiManager().openMain(player);
        }
    }
}
