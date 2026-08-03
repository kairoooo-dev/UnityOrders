package com.unity.orders.commands;

import com.unity.orders.UnityOrders;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the <code>/orders</code> command &mdash; the primary player-facing
 * command for interacting with the marketplace GUI.
 */
public final class OrderCommand implements CommandExecutor, TabCompleter {

    private final UnityOrders plugin;

    public OrderCommand(@NotNull UnityOrders plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("unityorders.use")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            plugin.getGuiManager().openMain(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open", "menu", "gui" -> plugin.getGuiManager().openMain(player);
            case "myorders", "list" -> plugin.getGuiManager().openMyOrders(player, 0);
            case "browse" -> plugin.getGuiManager().openBrowse(player, 0);
            case "help" -> sendHelp(player);
            default -> {
                player.sendMessage("Unknown subcommand. Use /orders help for usage.");
                return true;
            }
        }

        return true;
    }

    private void sendHelp(@NotNull Player player) {
        player.sendMessage("=== UnityOrders Help ===");
        player.sendMessage("/orders - Open the marketplace GUI");
        player.sendMessage("/orders myorders - View your orders");
        player.sendMessage("/orders browse - Browse all orders");
        player.sendMessage("/orders help - Show this help");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String sub : List.of("open", "menu", "gui", "myorders", "list", "browse", "help")) {
                if (sub.startsWith(input)) {
                    suggestions.add(sub);
                }
            }
            return suggestions;
        }
        return List.of();
    }
}
