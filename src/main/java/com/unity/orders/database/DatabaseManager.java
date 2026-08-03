package com.unity.orders.database;

import com.unity.orders.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages database connections using HikariCP connection pooling.
 *
 * <p>Supports SQLite, MySQL, MariaDB, and PostgreSQL. All connections are
 * obtained from the pool and must be closed (try-with-resources) by the caller.</p>
 */
public final class DatabaseManager {

    private final @NotNull JavaPlugin plugin;
    private final @NotNull ConfigManager config;
    private final @NotNull DatabaseType databaseType;
    private volatile @NotNull HikariDataSource dataSource;

    public DatabaseManager(@NotNull JavaPlugin plugin, @NotNull ConfigManager config) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.databaseType = DatabaseType.fromName(config.getDatabaseType());
        this.dataSource = createDataSource();
    }

    /**
     * Returns a connection from the pool.
     *
     * <p>Callers must close the connection (try-with-resources).</p>
     *
     * @return a database connection
     * @throws SQLException if a connection cannot be obtained
     */
    public @NotNull Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Returns the configured database type.
     *
     * @return the database type
     */
    public @NotNull DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Returns whether the pool is running.
     *
     * @return {@code true} if the pool is active
     */
    public boolean isRunning() {
        return !dataSource.isClosed();
    }

    /**
     * Closes the connection pool and releases all resources.
     */
    public void shutdown() {
        if (!dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }

    // ── Internal ─────────────────────────────────────────────────────

    private @NotNull HikariDataSource createDataSource() {
        HikariConfig hikariConfig = new HikariConfig();

        switch (databaseType) {
            case SQLITE -> configureSqlite(hikariConfig);
            case MYSQL -> configureMysql(hikariConfig);
            case MARIADB -> configureMariadb(hikariConfig);
            case POSTGRESQL -> configurePostgresql(hikariConfig);
        }

        // Pool settings
        int poolSize = config.getDatabasePoolSize();
        if (databaseType == DatabaseType.SQLITE) {
            // SQLite should use a small pool due to single-writer limitation
            poolSize = Math.min(poolSize, 1);
        }
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setConnectionTimeout(10_000L);
        hikariConfig.setIdleTimeout(TimeUnit.MINUTES.toMillis(10));
        hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
        hikariConfig.setKeepaliveTime(TimeUnit.MINUTES.toMillis(5));
        hikariConfig.setPoolName("UnityOrders-HikariPool");

        // Leak detection
        hikariConfig.setLeakDetectionThreshold(60_000L);

        plugin.getLogger().info("Database pool initialised: " + databaseType.getDisplayName());

        return new HikariDataSource(hikariConfig);
    }

    private void configureSqlite(@NotNull HikariConfig hikariConfig) {
        String dbFile = plugin.getDataFolder().toPath().resolve("data.db").toString();
        plugin.getDataFolder().mkdirs();
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile);
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setPoolName("UnityOrders-SQLite");
        // SQLite-specific optimisations
        hikariConfig.addDataSourceProperty("journal_mode", "WAL");
        hikariConfig.addDataSourceProperty("foreign_keys", "true");
        hikariConfig.addDataSourceProperty("busy_timeout", "5000");
    }

    private void configureMysql(@NotNull HikariConfig hikariConfig) {
        String url = String.format("jdbc:mysql://%s:%d/%s",
                config.getDatabaseHost(),
                config.getDatabasePort(),
                config.getDatabaseName());
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername(config.getDatabaseUser());
        hikariConfig.setPassword(config.getDatabasePassword());
        hikariConfig.addDataSourceProperty("useSSL", "false");
        hikariConfig.addDataSourceProperty("useUnicode", "true");
        hikariConfig.addDataSourceProperty("characterEncoding", "UTF-8");
        hikariConfig.addDataSourceProperty("serverTimezone", "UTC");
        hikariConfig.addDataSourceProperty("allowPublicKeyRetrieval", "true");
    }

    private void configureMariadb(@NotNull HikariConfig hikariConfig) {
        String url = String.format("jdbc:mariadb://%s:%d/%s",
                config.getDatabaseHost(),
                config.getDatabasePort(),
                config.getDatabaseName());
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setUsername(config.getDatabaseUser());
        hikariConfig.setPassword(config.getDatabasePassword());
        hikariConfig.addDataSourceProperty("useUnicode", "true");
        hikariConfig.addDataSourceProperty("characterEncoding", "UTF-8");
    }

    private void configurePostgresql(@NotNull HikariConfig hikariConfig) {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                config.getDatabaseHost(),
                config.getDatabasePort(),
                config.getDatabaseName());
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setUsername(config.getDatabaseUser());
        hikariConfig.setPassword(config.getDatabasePassword());
    }
}
