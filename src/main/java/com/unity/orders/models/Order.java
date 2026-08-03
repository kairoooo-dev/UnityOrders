package com.unity.orders.models;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable domain model representing a single order in the UnityOrders system.
 *
 * <p>This class is thread-safe by virtue of immutability. All fields are final and the
 * object is published safely via the repository layer.</p>
 */
public final class Order {

    private final long id;
    private final @NotNull UUID playerId;
    private final @NotNull String playerName;
    private final @NotNull String material;
    private final int amount;
    private final double pricePerUnit;
    private final @NotNull OrderStatus status;
    private final @NotNull Instant createdAt;
    private final @Nullable Instant updatedAt;
    private final @Nullable String deliveredBy;

    private Order(long id, @NotNull UUID playerId, @NotNull String playerName,
                  @NotNull String material, int amount, double pricePerUnit,
                  @NotNull OrderStatus status, @NotNull Instant createdAt,
                  @Nullable Instant updatedAt, @Nullable String deliveredBy) {
        this.id = id;
        this.playerId = playerId;
        this.playerName = playerName;
        this.material = material;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deliveredBy = deliveredBy;
    }

    public long getId() {
        return id;
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull String getPlayerName() {
        return playerName;
    }

    public @NotNull String getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public double getTotalPrice() {
        return amount * pricePerUnit;
    }

    public @NotNull OrderStatus getStatus() {
        return status;
    }

    public @NotNull Instant getCreatedAt() {
        return createdAt;
    }

    public @Nullable Instant getUpdatedAt() {
        return updatedAt;
    }

    public @Nullable String getDeliveredBy() {
        return deliveredBy;
    }

    /**
     * Returns a new {@link Order} with the status updated and updatedAt set to now.
     *
     * @param newStatus the new status
     * @return a new order instance
     */
    public @NotNull Order withStatus(@NotNull OrderStatus newStatus) {
        return new Order(id, playerId, playerName, material, amount, pricePerUnit,
                newStatus, createdAt, Instant.now(), deliveredBy);
    }

    /**
     * Returns a new {@link Order} with the deliveredBy field set and status updated to DELIVERED.
     *
     * @param deliveredBy the name of the player who delivered the order
     * @return a new order instance
     */
    public @NotNull Order withDeliveredBy(@NotNull String deliveredBy) {
        return new Order(id, playerId, playerName, material, amount, pricePerUnit,
                OrderStatus.DELIVERED, createdAt, Instant.now(), deliveredBy);
    }

    /**
     * Creates a new builder for constructing an {@link Order}.
     *
     * @return a new builder
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link Order}.
     */
    public static final class Builder {
        private long id;
        private UUID playerId;
        private String playerName;
        private String material;
        private int amount;
        private double pricePerUnit;
        private OrderStatus status = OrderStatus.PENDING;
        private Instant createdAt = Instant.now();
        private Instant updatedAt;
        private String deliveredBy;

        private Builder() {
        }

        public @NotNull Builder id(long id) {
            this.id = id;
            return this;
        }

        public @NotNull Builder playerId(@NotNull UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        public @NotNull Builder playerName(@NotNull String playerName) {
            this.playerName = playerName;
            return this;
        }

        public @NotNull Builder material(@NotNull String material) {
            this.material = material;
            return this;
        }

        public @NotNull Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public @NotNull Builder pricePerUnit(double pricePerUnit) {
            this.pricePerUnit = pricePerUnit;
            return this;
        }

        public @NotNull Builder status(@NotNull OrderStatus status) {
            this.status = status;
            return this;
        }

        public @NotNull Builder createdAt(@NotNull Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public @NotNull Builder updatedAt(@Nullable Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public @NotNull Builder deliveredBy(@Nullable String deliveredBy) {
            this.deliveredBy = deliveredBy;
            return this;
        }

        public @NotNull Order build() {
            Objects.requireNonNull(playerId, "playerId must not be null");
            Objects.requireNonNull(playerName, "playerName must not be null");
            Objects.requireNonNull(material, "material must not be null");
            Objects.requireNonNull(status, "status must not be null");
            Objects.requireNonNull(createdAt, "createdAt must not be null");

            if (amount <= 0) {
                throw new IllegalArgumentException("amount must be positive");
            }
            if (pricePerUnit < 0) {
                throw new IllegalArgumentException("pricePerUnit must not be negative");
            }

            return new Order(id, playerId, playerName, material, amount, pricePerUnit,
                    status, createdAt, updatedAt, deliveredBy);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", material='" + material + '\'' +
                ", amount=" + amount +
                ", pricePerUnit=" + pricePerUnit +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deliveredBy='" + deliveredBy + '\'' +
                '}';
    }
}
