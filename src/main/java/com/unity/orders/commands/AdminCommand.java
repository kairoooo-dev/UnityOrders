package com.unity.orders.commands;

import com.unity.orders.UnityOrders;
import com.unity.orders.managers.OrderManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles the <code>/unityorders</code> admin command.
 *
 * <p>Requires the <code>unityorders.admin</code> permission.
 * Supports reload, view, cancel, and stats subcommands.</p>
 */
public final class AdminCommand implements CommandExecutor, TabCompleter {

    private final UnityOrders plugin;

    public AdminCommand(@NotNull UnityOrders plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("unityorders.admin")) {
            sender.sendMessage("You do not have permission for this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload(sender);
            }
            case "stats" -> {
                List<OrderManager.BuyOrder> all = plugin.getOrderManager().getAllOrders();
                sender.sendMessage("=== UnityOrders Stats ===");
                sender.sendMessage("Total orders: " + all.size());
                long fulfilled = all.stream().filter(OrderManager.BuyOrder::isFulfilled).count();
                sender.sendMessage("Fulfilled: " + fulfilled);
                sender.sendMessage("Active: " + (all.size() - fulfilled));
            }
            case "cancel" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /unityorders cancel <orderId>");
                    return true;
                }
                try {
                    long orderId = Long.parseLong(args[1]);
                    OrderManager.BuyOrder order = plugin.getOrderManager().findOrder(orderId);
                    if (order == null) {
                        sender.sendMessage("Order not found.");
                        return true;
                    }
                    OfflinePlayer buyer = Bukkit.getOfflinePlayer(order.getBuyerUUID());
                    if (buyer.isOnline() && buyer.getPlayer() != null) {
                        plugin.getOrderManager().cancelOrder(orderId, buyer.getPlayer());
                    } else {
                        sender.sendMessage("Buyer is offline. Order cancelled without refund.");
                        // Force remove
                    }
                    sender.sendMessage("Order #" + orderId + " cancelled.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid order ID.");
                }
            }
            case "view" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /unityorders view <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                UUID uuid = target.getUniqueId();
                List<OrderManager.BuyOrder> orders = plugin.getOrderManager().getOrders(uuid);
                if (orders.isEmpty()) {
                    sender.sendMessage("No orders found for " + args[1]);
                    return true;
                }
                sender.sendMessage("=== Orders for " + args[1] + " ===");
                for (OrderManager.BuyOrder order : orders) {
                    sender.sendMessage("#" + order.getOrderId() + " | " + order.getMaterial() + " | " +
                            order.getRemaining() + "/" + order.getTotalAmount() + " | " + order.getPricePerUnit() + " each");
                }
            }
            case "help" -> sendHelp(sender);
            default -> {
                sender.sendMessage("Unknown subcommand. Use /unityorders help for usage.");
            }
        }

        return true;
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage("=== UnityOrders Admin ===");
        sender.sendMessage("/unityorders reload - Reload configuration");
        sender.sendMessage("/unityorders stats - View order statistics");
        sender.sendMessage("/unityorders cancel <orderId> - Cancel an order");
        sender.sendMessage("/unityorders view <player> - View a player's orders");
        sender.sendMessage("/unityorders help - Show this help");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("unityorders.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String sub : List.of("reload", "stats", "cancel", "view", "help")) {
                if (sub.startsWith(input)) {
                    suggestions.add(sub);
                }
            }
            return suggestions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            List<String> names = new ArrayList<>();
            for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                if (p.getName() != null && p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    names.add(p.getName());
                }
            }
            return names;
        }

        return List.of();
    }
}
