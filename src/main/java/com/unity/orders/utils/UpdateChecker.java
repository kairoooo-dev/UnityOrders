package com.unity.orders.utils;

import com.unity.orders.UnityOrders;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Asynchronous update checker for UnityOrders.
 *
 * <p>Fetches the latest release tag from the configured GitHub repository
 * and compares it against the running version. Results are cached and
 * accessible via {@link #isUpdateAvailable()}.</p>
 */
public final class UpdateChecker {

    private final UnityOrders plugin;
    private volatile boolean updateAvailable = false;
    private volatile String latestVersion = "";
    private volatile String downloadUrl = "";

    public UpdateChecker(@NotNull UnityOrders plugin) {
        this.plugin = plugin;
    }

    /**
     * Asynchronously checks for updates.
     *
     * @return a future that completes when the check is done
     */
    @NotNull
    public CompletableFuture<Void> checkAsync() {
        return CompletableFuture.runAsync(this::check);
    }

    private void check() {
        String repoUrl = plugin.getConfig().getString("update-checker.url",
                "https://api.github.com/repos/kairoooo-dev/UnityOrders/releases/latest");

        try {
            URI uri = new URI(repoUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "UnityOrders-UpdateChecker");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) {
                plugin.getLogger().log(Level.WARNING, "Update check failed: HTTP " + conn.getResponseCode());
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                latestVersion = json.get("tag_name").getAsString().replace("v", "");
                if (json.has("html_url")) {
                    downloadUrl = json.get("html_url").getAsString();
                }

                String currentVersion = plugin.getPluginMeta().getVersion();
                updateAvailable = compareVersions(latestVersion, currentVersion) > 0;

                if (updateAvailable) {
                    plugin.getLogger().info("Update available: v" + latestVersion + " (current: v" + currentVersion + ")");
                } else {
                    plugin.getLogger().info("You are running the latest version of UnityOrders.");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check for updates", e);
        }
    }

    /**
     * Compares two semantic version strings.
     *
     * @param v1 the first version
     * @param v2 the second version
     * @return positive if v1 > v2, negative if v1 < v2, zero if equal
     */
    static int compareVersions(@NotNull String v1, @NotNull String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int maxLen = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLen; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }

    public boolean isUpdateAvailable() { return updateAvailable; }
    @NotNull public String getLatestVersion() { return latestVersion; }
    @NotNull public String getDownloadUrl() { return downloadUrl; }
}
