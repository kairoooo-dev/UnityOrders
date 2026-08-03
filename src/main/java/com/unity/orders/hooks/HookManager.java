package com.unity.orders.hooks;

import com.unity.orders.UnityOrders;
import com.unity.orders.hooks.papi.PlaceholderAPIHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Manages detection and integration with optional soft-depend plugins.
 *
 * <p>Detects: Vault (economy), PlaceholderAPI, LuckPerms.
 * Each hook is initialized only if the corresponding plugin is present,
 * and gracefully skipped otherwise.</p>
 */
public final class HookManager {

    private final UnityOrders plugin;

    private Economy economy;
    private boolean vaultEnabled = false;
    private boolean papiEnabled = false;
    private boolean luckPermsEnabled = false;

    public HookManager(@NotNull UnityOrders plugin) {
        this.plugin = plugin;
    }

    /**
     * Detects and initializes all available hooks.
     */
    public void detectHooks() {
        // Vault
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            try {
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    economy = rsp.getProvider();
                    vaultEnabled = economy != null;
                    plugin.getLogger().info("Vault economy hooked: " + (economy != null ? economy.getName() : "null"));
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to hook Vault economy", e);
            }
        }

        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new PlaceholderAPIHook(plugin).register();
                papiEnabled = true;
                plugin.getLogger().info("PlaceholderAPI hooked successfully.");
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to hook PlaceholderAPI", e);
            }
        }

        // LuckPerms
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            luckPermsEnabled = true;
            plugin.getLogger().info("LuckPerms detected.");
        }
    }

    @Nullable
    public Economy getEconomy() { return economy; }

    public boolean isVaultEnabled() { return vaultEnabled; }
    public boolean isPapiEnabled() { return papiEnabled; }
    public boolean isLuckPermsEnabled() { return luckPermsEnabled; }
}
