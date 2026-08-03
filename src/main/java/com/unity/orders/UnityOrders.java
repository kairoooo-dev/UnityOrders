package com.unity.orders;

import com.unity.orders.api.UnityOrdersAPI;
import com.unity.orders.api.APIImplementation;
import com.unity.orders.commands.AdminCommand;
import com.unity.orders.commands.OrderCommand;
import com.unity.orders.gui.GuiManager;
import com.unity.orders.hooks.HookManager;
import com.unity.orders.listeners.GuiListener;
import com.unity.orders.listeners.PlayerListener;
import com.unity.orders.managers.OrderManager;
import com.unity.orders.utils.UpdateChecker;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * UnityOrders &mdash; enterprise-grade GUI-driven buy order marketplace.
 *
 * <p>Target: Paper 1.21.11+ (Folia-compatible). Economy via Vault,
 * permissions via LuckPerms, placeholders via PlaceholderAPI.</p>
 */
public final class UnityOrders extends JavaPlugin {

    private static UnityOrders instance;

    private BukkitAudiences adventure;
    private OrderManager orderManager;
    private GuiManager guiManager;
    private HookManager hookManager;
    private UpdateChecker updateChecker;
    private APIImplementation apiImplementation;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Adventure audiences
        this.adventure = BukkitAudiences.create(this);

        // Hook manager (soft-depend integrations)
        this.hookManager = new HookManager(this);
        this.hookManager.detectHooks();

        // Order manager (core business logic)
        this.orderManager = new OrderManager(this);
        this.orderManager.initialize();

        // GUI manager
        this.guiManager = new GuiManager(this);

        // API implementation
        this.apiImplementation = new APIImplementation(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Commands
        Objects.requireNonNull(getCommand("orders")).setExecutor(new OrderCommand(this));
        Objects.requireNonNull(getCommand("unityorders")).setExecutor(new AdminCommand(this));

        // Update checker
        this.updateChecker = new UpdateChecker(this);
        if (getConfig().getBoolean("update-checker.enabled", true)) {
            this.updateChecker.checkAsync();
        }

        getLogger().info("UnityOrders v" + getPluginMeta().getVersion() + " enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (this.orderManager != null) {
            this.orderManager.shutdown();
        }
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        instance = null;
        getLogger().info("UnityOrders disabled.");
    }

    /**
     * Reloads all configuration and runtime state.
     *
     * @param sender the sender initiating the reload
     */
    public void reload(@NotNull CommandSender sender) {
        reloadConfig();
        if (orderManager != null) {
            orderManager.reload();
        }
        sender.sendMessage("UnityOrders configuration reloaded.");
    }

    @NotNull
    public static UnityOrders getInstance() {
        return instance;
    }

    @NotNull
    public BukkitAudiences adventure() {
        return adventure;
    }

    @NotNull
    public OrderManager getOrderManager() {
        return orderManager;
    }

    @NotNull
    public GuiManager getGuiManager() {
        return guiManager;
    }

    @NotNull
    public HookManager getHookManager() {
        return hookManager;
    }

    @NotNull
    public UnityOrdersAPI getAPI() {
        return apiImplementation;
    }

    @NotNull
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
