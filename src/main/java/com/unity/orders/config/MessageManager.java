package com.unity.orders.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages localised messages using MiniMessage format.
 *
 * <p>Loads {@code messages.yml} from the plugin data folder and provides
 * cached {@link Component} accessors. Supports placeholder substitution
 * using {@code {placeholder}} syntax.</p>
 */
public final class MessageManager {

    private static final String MESSAGES_FILE_NAME = "messages.yml";

    private final @NotNull JavaPlugin plugin;
    private final @NotNull Path messagesPath;
    private final @NotNull MiniMessage miniMessage;
    private final @NotNull ConcurrentHashMap<String, String> rawCache = new ConcurrentHashMap<>();
    private final @NotNull ConcurrentHashMap<String, Component> componentCache = new ConcurrentHashMap<>();

    private volatile @NotNull YamlConfiguration messages;

    public MessageManager(@NotNull JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
        this.messagesPath = plugin.getDataFolder().toPath().resolve(MESSAGES_FILE_NAME);
        this.miniMessage = MiniMessage.miniMessage();
        this.messages = loadMessages();
        rebuildCache();
    }

    /**
     * Reloads messages from disk and rebuilds the cache.
     */
    public synchronized void reload() {
        this.messages = loadMessages();
        rebuildCache();
    }

    /**
     * Returns a parsed {@link Component} for the given key, with placeholders substituted.
     *
     * @param key          the message key
     * @param placeholders key-value pairs for substitution
     * @return the parsed component, or an empty component if the key is missing
     */
    public @NotNull Component get(@NotNull String key, @Nullable Map<String, String> placeholders) {
        String raw = rawCache.get(key);
        if (raw == null) {
            raw = messages.getString(key, "");
            if (raw.isEmpty()) {
                plugin.getLogger().warning("Missing message key: " + key);
                return Component.empty();
            }
            rawCache.putIfAbsent(key, raw);
        }

        String substituted = substitutePlaceholders(raw, placeholders);

        // Cache only the no-placeholder version
        if (placeholders == null || placeholders.isEmpty()) {
            return componentCache.computeIfAbsent(key, k -> miniMessage.deserialize(substituted));
        }

        return miniMessage.deserialize(substituted);
    }

    /**
     * Returns a parsed {@link Component} for the given key without placeholders.
     *
     * @param key the message key
     * @return the parsed component
     */
    public @NotNull Component get(@NotNull String key) {
        return get(key, null);
    }

    /**
     * Returns the raw string for the given key.
     *
     * @param key the message key
     * @return the raw string, or empty string if not found
     */
    public @NotNull String getRaw(@NotNull String key) {
        return rawCache.getOrDefault(key, messages.getString(key, ""));
    }

    /**
     * Returns a prefix component (usually a chat tag).
     *
     * @return the prefix component
     */
    public @NotNull Component getPrefix() {
        return get("general.prefix");
    }

    /**
     * Returns a prefixed message component.
     *
     * @param key          the message key
     * @param placeholders key-value pairs
     * @return the prefixed, parsed component
     */
    public @NotNull Component getPrefixed(@NotNull String key, @Nullable Map<String, String> placeholders) {
        Component prefix = getPrefix();
        Component message = get(key, placeholders);
        return prefix.append(Component.space()).append(message);
    }

    /**
     * Returns a prefixed message component without placeholders.
     *
     * @param key the message key
     * @return the prefixed, parsed component
     */
    public @NotNull Component getPrefixed(@NotNull String key) {
        return getPrefixed(key, null);
    }

    // ── Internal ─────────────────────────────────────────────────────

    private @NotNull String substitutePlaceholders(@NotNull String input, @Nullable Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) {
            return input;
        }
        String result = input;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private void rebuildCache() {
        rawCache.clear();
        componentCache.clear();
        for (String key : messages.getKeys(true)) {
            String value = messages.getString(key);
            if (value != null) {
                rawCache.put(key, value);
                componentCache.put(key, miniMessage.deserialize(value));
            }
        }
    }

    private @NotNull YamlConfiguration loadMessages() {
        if (!Files.exists(messagesPath)) {
            try {
                plugin.saveResource(MESSAGES_FILE_NAME, false);
            } catch (IllegalArgumentException e) {
                // Resource not bundled — create minimal default
                createDefaultMessages();
            }
        }
        return YamlConfiguration.loadConfiguration(messagesPath.toFile());
    }

    private void createDefaultMessages() {
        YamlConfiguration defaults = new YamlConfiguration();
        defaults.set("general.prefix", "<dark_gray>[<gradient:blue:aqua>UnityOrders</gradient>]</dark_gray>");
        defaults.set("general.no-permission", "<red>You do not have permission to do that.");
        defaults.set("general.player-only", "<red>This command can only be used by players.");
        defaults.set("general.unknown-command", "<red>Unknown subcommand. Use <white>/orders help</white> for help.");
        defaults.set("order.created", "<green>Order created successfully! ID: <white>{id}</white></green>");
        defaults.set("order.cancelled", "<yellow>Order {id} has been cancelled.");
        defaults.set("order.delivered", "<green>Order {id} delivered by {delivered_by}!</green>");
        defaults.set("order.not-found", "<red>Order not found.");
        defaults.set("order.expired", "<yellow>Order {id} has expired.");
        defaults.set("order.limit-reached", "<red>You have reached the maximum number of active orders.");
        defaults.set("error.invalid-amount", "<red>Invalid amount. Must be between 1 and 2304.");
        defaults.set("error.invalid-price", "<red>Invalid price. Must be a non-negative number.");
        defaults.set("error.invalid-material", "<red>Invalid material.");
        defaults.set("error.database", "<red>A database error occurred. Please try again later.");
        defaults.set("error.economy", "<red>An economy error occurred. Please contact an administrator.");

        try {
            Files.createDirectories(messagesPath.getParent());
            defaults.save(messagesPath.toFile());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default messages.yml: " + e.getMessage());
        }
    }
}
