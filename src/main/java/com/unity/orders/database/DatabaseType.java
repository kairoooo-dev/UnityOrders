package com.unity.orders.database;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Supported database types for UnityOrders.
 */
public enum DatabaseType {

    SQLITE("SQLite"),
    MYSQL("MySQL"),
    MARIADB("MariaDB"),
    POSTGRESQL("PostgreSQL");

    private final @NotNull String displayName;

    DatabaseType(@NotNull String displayName) {
        this.displayName = displayName;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the JDBC driver class name for this database type.
     *
     * @return the driver class name
     */
    public @NotNull String getDriverClassName() {
        return switch (this) {
            case SQLITE -> "org.sqlite.JDBC";
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case MARIADB -> "org.mariadb.jdbc.Driver";
            case POSTGRESQL -> "org.postgresql.Driver";
        };
    }

    /**
     * Returns whether this database type supports concurrent writers.
     *
     * @return {@code true} if concurrent writes are supported
     */
    public boolean supportsConcurrentWrites() {
        return this != SQLITE;
    }

    /**
     * Parses a database type from a config string.
     *
     * @param name the type name (case-insensitive)
     * @return the matching database type, defaults to {@link #SQLITE}
     */
    public static @NotNull DatabaseType fromName(@NotNull String name) {
        Objects.requireNonNull(name, "name must not be null");
        String normalised = name.trim().toLowerCase();
        return switch (normalised) {
            case "mysql" -> MYSQL;
            case "mariadb" -> MARIADB;
            case "postgres", "postgresql", "psql" -> POSTGRESQL;
            default -> SQLITE;
        };
    }
}
