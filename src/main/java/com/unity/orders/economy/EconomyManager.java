package com.unity.orders.economy;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages economy operations via Vault.
 *
 * <p>All operations are performed asynchronously where possible. If Vault
 * is not installed, the manager operates in a no-op mode and all operations
 * return {@code false}.</p>
 */
public final class EconomyManager {

    private final @NotNull JavaPlugin plugin;
    private volatile @Nullable Economy economy;
    private final boolean enabled;

    public EconomyManager(@NotNull JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
        this.economy = setupEconomy();
        this.enabled = economy != null;

        if (!enabled) {
            plugin.getLogger().warning("Vault economy not found. Economy features will be disabled.");
        } else {
            plugin.getLogger().info("Vault economy hooked: " + economy.getName());
        }
    }

    /**
     * Returns whether the economy is available.
     *
     * @return {@code true} if Vault economy is hooked
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks whether a player has at least the given amount.
     *
     * @param player the player
     * @param amount the amount
     * @return {@code true} if the player has enough balance
     */
    public boolean has(@NotNull OfflinePlayer player, double amount) {
        if (!enabled || economy == null) return false;
        return economy.has(player, amount);
    }

    /**
     * Returns the player's balance.
     *
     * @param player the player
     * @return the balance, or 0 if economy is disabled
     */
    public double getBalance(@NotNull OfflinePlayer player) {
        if (!enabled || economy == null) return 0;
        return economy.getBalance(player);
    }

    /**
     * Withdraws an amount from the player's account.
     *
     * @param player the player
     * @param amount the amount to withdraw
     * @return {@code true} if the transaction was successful
     */
    public boolean withdraw(@NotNull OfflinePlayer player, double amount) {
        if (!enabled || economy == null) return false;
        if (amount <= 0) return false;
        var result = economy.withdrawPlayer(player, amount);
        return result.transactionSuccess();
    }

    /**
     * Deposits an amount into the player's account.
     *
     * @param player the player
     * @param amount the amount to deposit
     * @return {@code true} if the transaction was successful
     */
    public boolean deposit(@NotNull OfflinePlayer player, double amount) {
        if (!enabled || economy == null) return false;
        if (amount <= 0) return false;
        var result = economy.depositPlayer(player, amount);
        return result.transactionSuccess();
    }

    /**
     * Withdraws an amount asynchronously.
     *
     * @param player the player
     * @param amount the amount
     * @return a future that completes with {@code true} on success
     */
    public @NotNull CompletableFuture<Boolean> withdrawAsync(@NotNull OfflinePlayer player, double amount) {
        return CompletableFuture.supplyAsync(() -> withdraw(player, amount));
    }

    /**
     * Deposits an amount asynchronously.
     *
     * @param player the player
     * @param amount the amount
     * @return a future that completes with {@code true} on success
     */
    public @NotNull CompletableFuture<Boolean> depositAsync(@NotNull OfflinePlayer player, double amount) {
        return CompletableFuture.supplyAsync(() -> deposit(player, amount));
    }

    /**
     * Transfers an amount from one player to another.
     *
     * @param from   the source player
     * @param to     the target player
     * @param amount the amount
     * @return {@code true} if the transfer was successful
     */
    public boolean transfer(@NotNull OfflinePlayer from, @NotNull OfflinePlayer to, double amount) {
        if (!enabled || economy == null) return false;
        if (amount <= 0) return false;
        if (!economy.has(from, amount)) return false;

        var withdrawResult = economy.withdrawPlayer(from, amount);
        if (!withdrawResult.transactionSuccess()) return false;

        var depositResult = economy.depositPlayer(to, amount);
        if (!depositResult.transactionSuccess()) {
            // Rollback withdrawal
            economy.depositPlayer(from, amount);
            return false;
        }
        return true;
    }

    // ── Internal ─────────────────────────────────────────────────────

    private @Nullable Economy setupEconomy() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return null;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return null;
        }
        return rsp.getProvider();
    }
}
