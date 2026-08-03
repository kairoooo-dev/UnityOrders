package com.unity.orders.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles database schema migrations for UnityOrders.
 *
 * <p>Migrations are versioned and applied in order. A {@code schema_migrations}
 * table tracks which migrations have been applied. All migrations are
 * idempotent and safe to re-run.</p>
 */
public final class DatabaseMigration {

    private static final String MIGRATIONS_TABLE = "schema_migrations";
    private static final int LATEST_VERSION = 1;

    private final @NotNull JavaPlugin plugin;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull DatabaseType databaseType;

    public DatabaseMigration(@NotNull JavaPlugin plugin, @NotNull DatabaseManager databaseManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager must not be null");
        this.databaseType = databaseManager.getDatabaseType();
    }

    /**
     * Runs all pending migrations.
     *
     * @throws SQLException if a migration fails
     */
    public void migrate() throws SQLException {
        ensureMigrationsTable();

        int currentVersion = getCurrentVersion();
        if (currentVersion >= LATEST_VERSION) {
            plugin.getLogger().info("Database schema is up to date (version " + currentVersion + ").");
            return;
        }

        List<Migration> migrations = getMigrations();

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Migration migration : migrations) {
                    if (migration.version > currentVersion) {
                        plugin.getLogger().info("Applying migration v" + migration.version + ": " + migration.description);
                        migration.apply(conn, databaseType);
                        recordMigration(conn, migration.version, migration.description);
                    }
                }
                conn.commit();
                plugin.getLogger().info("Database migrations complete. Now at version " + LATEST_VERSION + ".");
            } catch (SQLException e) {
                conn.rollback();
                plugin.getLogger().severe("Migration failed, rolling back: " + e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Returns the current schema version.
     *
     * @return the version, or 0 if no migrations have been applied
     */
    public int getCurrentVersion() {
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT MAX(version) AS version FROM " + MIGRATIONS_TABLE)) {
            if (rs.next()) {
                return rs.getInt("version");
            }
            return 0;
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get current migration version: " + e.getMessage());
            return 0;
        }
    }

    // ── Internal ─────────────────────────────────────────────────────

    private void ensureMigrationsTable() {
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS """ + MIGRATIONS_TABLE + """ (
                        version INT NOT NULL PRIMARY KEY,
                        description VARCHAR(255) NOT NULL,
                        applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create migrations table: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void recordMigration(@NotNull Connection conn, int version, @NotNull String description) throws SQLException {
        try (var ps = conn.prepareStatement(
                "INSERT INTO " + MIGRATIONS_TABLE + " (version, description) VALUES (?, ?)")) {
            ps.setInt(1, version);
            ps.setString(2, description);
            ps.executeUpdate();
        }
    }

    private @NotNull List<Migration> getMigrations() {
        List<Migration> migrations = new ArrayList<>();
        migrations.add(new Migration(1, "Initial schema", this::migrationV1));
        return migrations;
    }

    // ── Migration implementations ────────────────────────────────────

    private void migrationV1(@NotNull Connection conn, @NotNull DatabaseType type) throws SQLException {
        String autoIncrement = type == DatabaseType.POSTGRESQL ? "SERIAL" : "AUTOINCREMENT";
        String textType = type == DatabaseType.POSTGRESQL ? "TEXT" : "VARCHAR(255)";
        String timestampDefault = type == DatabaseType.POSTGRESQL
                ? "TIMESTAMP NOT NULL DEFAULT NOW()"
                : "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP";

        try (Statement stmt = conn.createStatement()) {
            // Orders table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id INTEGER PRIMARY KEY """ + autoIncrement + """,
                        player_uuid """ + textType + """ NOT NULL,
                        player_name """ + textType + """ NOT NULL,
                        material """ + textType + """ NOT NULL,
                        amount INTEGER NOT NULL CHECK (amount > 0),
                        price_per_unit DOUBLE PRECISION NOT NULL CHECK (price_per_unit >= 0),
                        status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
                        created_at """ + timestampDefault + """,
                        updated_at TIMESTAMP,
                        delivered_by """ + textType + """
                    )
                    """);

            // Indexes for common queries
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_player_uuid ON orders(player_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_material ON orders(material)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at)");
        }
    }

    // ── Migration functional interface ───────────────────────────────

    @FunctionalInterface
    private interface MigrationStep {
        void apply(@NotNull Connection conn, @NotNull DatabaseType type) throws SQLException;
    }

    private record Migration(int version, String description, @NotNull MigrationStep step) {
        void apply(@NotNull Connection conn, @NotNull DatabaseType type) throws SQLException {
            step.apply(conn, type);
        }
    }
}
