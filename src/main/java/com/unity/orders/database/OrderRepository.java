package com.unity.orders.database;

import com.unity.orders.models.Order;
import com.unity.orders.models.OrderCreateRequest;
import com.unity.orders.models.OrderStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Repository for {@link Order} persistence.
 *
 * <p>All database operations are performed asynchronously using
 * {@link CompletableFuture}. The repository uses prepared statements
 * to prevent SQL injection and handles connection lifecycle internally.</p>
 */
public final class OrderRepository {

    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull Executor asyncExecutor;

    public OrderRepository(@NotNull DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager must not be null");
        this.asyncExecutor = ForkJoinPool.commonPool();
    }

    /**
     * Creates a new order in the database.
     *
     * @param request the create request
     * @return a future that completes with the created order
     */
    public @NotNull CompletableFuture<Order> create(@NotNull OrderCreateRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                    INSERT INTO orders (player_uuid, player_name, material, amount, price_per_unit, status)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
            try (Connection conn = databaseManager.getConnection();
                 var ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, request.getPlayerId().toString());
                ps.setString(2, request.getPlayerName());
                ps.setString(3, request.getMaterial());
                ps.setInt(4, request.getAmount());
                ps.setDouble(5, request.getPricePerUnit());
                ps.setString(6, OrderStatus.PENDING.name());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        return Order.builder()
                                .id(id)
                                .playerId(request.getPlayerId())
                                .playerName(request.getPlayerName())
                                .material(request.getMaterial())
                                .amount(request.getAmount())
                                .pricePerUnit(request.getPricePerUnit())
                                .status(OrderStatus.PENDING)
                                .build();
                    }
                    throw new SQLException("Failed to retrieve generated order ID");
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create order", e);
            }
        }, asyncExecutor);
    }

    /**
     * Finds an order by ID.
     *
     * @param id the order ID
     * @return a future that completes with the order, or empty if not found
     */
    public @NotNull CompletableFuture<Optional<Order>> findById(long id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM orders WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find order by ID: " + id, e);
            }
        }, asyncExecutor);
    }

    /**
     * Finds all active orders for a player.
     *
     * @param playerId the player UUID
     * @return a future that completes with a list of active orders
     */
    public @NotNull CompletableFuture<List<Order>> findActiveByPlayer(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM orders WHERE player_uuid = ? AND status IN ('PENDING', 'ACTIVE') ORDER BY created_at DESC";
            try (Connection conn = databaseManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                return mapRows(ps);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find active orders for player: " + playerId, e);
            }
        }, asyncExecutor);
    }

    /**
     * Finds all orders with a given status.
     *
     * @param status the order status
     * @return a future that completes with a list of orders
     */
    public @NotNull CompletableFuture<List<Order>> findByStatus(@NotNull OrderStatus status) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM orders WHERE status = ? ORDER BY created_at DESC";
            try (Connection conn = databaseManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setString(1, status.name());
                return mapRows(ps);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find orders by status: " + status, e);
            }
        }, asyncExecutor);
    }

    /**
     * Counts active orders for a player.
     *
     * @param playerId the player UUID
     * @return a future that completes with the count
     */
    public @NotNull CompletableFuture<Integer> countActiveByPlayer(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM orders WHERE player_uuid = ? AND status IN ('PENDING', 'ACTIVE')";
            try (Connection conn = databaseManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to count active orders for player: " + playerId, e);
            }
        }, asyncExecutor);
    }

    /**
     * Updates the status of an order.
     *
     * @param id        the order ID
     * @param newStatus the new status
     * @return a future that completes with {@code true} if the update was successful
     */
    public @NotNull CompletableFuture<Boolean> updateStatus(long id, @NotNull OrderStatus newStatus) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setString(1, newStatus.name());
                ps.setTimestamp(2, Timestamp.from(Instant.now()));
                ps.setLong(3, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update order status: " + id, e);
            }
        }, asyncExecutor);
    }

    /**
     * Marks an order as delivered.
     *
     * @param id          the order ID
     * @param deliveredBy the name of the delivering player
     * @return a future that completes with {@code true} if successful
     */
    public @NotNull CompletableFuture<Boolean> markDelivered(long id, @NotNull String deliveredBy) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE orders SET status = 'DELIVERED', delivered_by = ?, updated_at = ? WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setString(1, deliveredBy);
                ps.setTimestamp(2, Timestamp.from(Instant.now()));
                ps.setLong(3, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to mark order as delivered: " + id, e);
            }
        }, asyncExecutor);
    }

    /**
     * Expires orders that are older than the given threshold.
     *
     * @param expirySeconds the expiry threshold in seconds
     * @return a future that completes with the number of expired orders
     */
    public @NotNull CompletableFuture<Integer> expireOldOrders(long expirySeconds) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE orders SET status = 'EXPIRED', updated_at = ? WHERE status IN ('PENDING', 'ACTIVE') AND created_at < ?";
            try (Connection conn = databaseManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                Instant now = Instant.now();
                Instant threshold = now.minusSeconds(expirySeconds);
                ps.setTimestamp(1, Timestamp.from(now));
                ps.setTimestamp(2, Timestamp.from(threshold));
                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to expire old orders", e);
            }
        }, asyncExecutor);
    }

    // ── Row mapping ──────────────────────────────────────────────────

    private @NotNull Order mapRow(@NotNull ResultSet rs) throws SQLException {
        Order.Builder builder = Order.builder()
                .id(rs.getLong("id"))
                .playerId(UUID.fromString(rs.getString("player_uuid")))
                .playerName(rs.getString("player_name"))
                .material(rs.getString("material"))
                .amount(rs.getInt("amount"))
                .pricePerUnit(rs.getDouble("price_per_unit"))
                .status(OrderStatus.valueOf(rs.getString("status")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            builder.createdAt(createdAt.toInstant());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            builder.updatedAt(updatedAt.toInstant());
        }

        String deliveredBy = rs.getString("delivered_by");
        if (deliveredBy != null) {
            builder.deliveredBy(deliveredBy);
        }

        return builder.build();
    }

    private @NotNull List<Order> mapRows(@NotNull PreparedStatement ps) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        }
        return orders;
    }
}
