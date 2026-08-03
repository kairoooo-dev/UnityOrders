package com.unity.orders.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Centralised configuration manager for UnityOrders.
 *
 * <p>Loads {@code config.yml} from the plugin data folder, supports hot-reloading,
 * and provides typed accessors for all configuration values. All public methods
 * are thread-safe.</p>
 */
public final class ConfigManager {

    private static final String CONFIG_FILE_NAME = "config.yml";
    private static final int CURRENT_CONFIG_VERSION = 1;

    private final @NotNull JavaPlugin plugin;
    private final @NotNull Path configPath;
    private final @NotNull CopyOnWriteArrayList<Runnable> reloadListeners = new CopyOnWriteArrayList<>();
    private volatile @NotNull YamlConfiguration config;

    public ConfigManager(@NotNull JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
        this.configPath = plugin.getDataFolder().toPath().resolve(CONFIG_FILE_NAME);
        this.config = loadConfig();
    }

    /**
     * Loads (or reloads) the configuration from disk.
     *
     * <p>If the file does not exist, the default resource is saved first.
     * If the config version is outdated, a migration is attempted.</p>
     */
    public synchronized void reload() {
        this.config = loadConfig();
        reloadListeners.forEach(Runnable::run);
    }

    /**
     * Registers a listener that is called whenever the config is reloaded.
     *
     * @param listener the listener to register
     */
    public void addReloadListener(@NotNull Runnable listener) {
        reloadListeners.add(Objects.requireNonNull(listener, "listener must not be null"));
    }

    /**
     * Returns the raw {@link YamlConfiguration}.
     *
     * @return the current configuration
     */
    public @NotNull YamlConfiguration getConfig() {
        return config;
    }

    // ── Typed accessors ──────────────────────────────────────────────

    /**
     * Gets a string value from the config.
     *
     * @param path the config path
     * @param def  the default value
     * @return the value or default
     */
    public @NotNull String getString(@NotNull String path, @NotNull String def) {
        return config.getString(path, def);
    }

    /**
     * Gets an integer value from the config.
     *
     * @param path the config path
     * @param def  the default value
     * @return the value or default
     */
    public int getInt(@NotNull String path, int def) {
        return config.getInt(path, def);
    }

    /**
     * Gets a double value from the config.
     *
     * @param path the config path
     * @param def  the default value
     * @return the value or default
     */
    public double getDouble(@NotNull String path, double def) {
        return config.getDouble(path, def);
    }

    /**
     * Gets a boolean value from the config.
     *
     * @param path the config path
     * @param def  the default value
     * @return the value or default
     */
    public boolean getBoolean(@NotNull String path, boolean def) {
        return config.getBoolean(path, def);
    }

    /**
     * Gets a string list from the config.
     *
     * @param path the config path
     * @return an unmodifiable list (empty if not found)
     */
    public @NotNull @Unmodifiable List<String> getStringList(@NotNull String path) {
        List<String> list = config.getStringList(path);
        return Collections.unmodifiableList(list);
    }

    /**
     * Gets a configuration section from the config.
     *
     * @param path the config path
     * @return the section or {@code null}
     */
    public @Nullable ConfigurationSection getSection(@NotNull String path) {
        return config.getConfigurationSection(path);
    }

    // ── Domain-specific config ───────────────────────────────────────

    /**
     * Returns the maximum number of active orders a player can have.
     *
     * @return the max active orders
     */
    public int getMaxActiveOrders() {
        return getInt("orders.max-active-per-player", 10);
    }

    /**
     * Returns the order expiry time in seconds.
     *
     * @return the expiry time in seconds
     */
    public long getOrderExpirySeconds() {
        return getLong("orders.expiry-seconds", 86400L);
    }

    /**
     * Returns the database type configured.
     *
     * @return the database type string
     */
    public @NotNull String getDatabaseType() {
        return getString("database.type", "sqlite");
    }

    /**
     * Returns the database host.
     *
     * @return the host
     */
    public @NotNull String getDatabaseHost() {
        return getString("database.host", "localhost");
    }

    /**
     * Returns the database port.
     *
     * @return the port
     */
    public int getDatabasePort() {
        return getInt("database.port", 3306);
    }

    /**
     * Returns the database name.
     *
     * @return the database name
     */
    public @NotNull String getDatabaseName() {
        return getString("database.name", "unityorders");
    }

    /**
     * Returns the database username.
     *
     * @return the username
     */
    public @NotNull String getDatabaseUser() {
        return getString("database.user", "root");
    }

    /**
     * Returns the database password.
     *
     * @return the password
     */
    public @NotNull String getDatabasePassword() {
        return getString("database.password", "");
    }

    /**
     * Returns the connection pool size.
     *
     * @return the pool size
     */
    public int getDatabasePoolSize() {
        return getInt("database.pool-size", 10);
    }

    /**
     * Returns whether Folia scheduler should be used.
     *
     * @return {@code true} if Folia is enabled
     */
    public boolean isFoliaEnabled() {
        return getBoolean("scheduler.folia", false);
    }

    /**
     * Returns whether debug mode is enabled.
     *
     * @return {@code true} if debug is enabled
     */
    public boolean isDebugEnabled() {
        return getBoolean("debug", false);
    }

    // ── Internal helpers ─────────────────────────────────────────────

    private long getLong(@NotNull String path, long def) {
        return config.getLong(path, def);
    }

    private @NotNull YamlConfiguration loadConfig() {
        if (!Files.exists(configPath)) {
            plugin.saveDefaultConfig();
        }
        YamlConfiguration loaded = YamlConfiguration.loadConfiguration(configPath.toFile());

        int version = loaded.getInt("config-version", 0);
        if (version < CURRENT_CONFIG_VERSION) {
            loaded.set("config-version", CURRENT_CONFIG_VERSION);
            try {
                loaded.save(configPath.toFile());
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save migrated config: " + e.getMessage());
            }
        }

        return loaded;
    }
}
