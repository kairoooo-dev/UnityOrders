package com.unity.orders.models;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable request object for creating a new order.
 *
 * <p>This object is validated before being passed to the repository layer.
 * It carries only the data supplied by the player — server-assigned fields
 * (id, createdAt, status) are set during persistence.</p>
 */
public final class OrderCreateRequest {

    private final @NotNull UUID playerId;
    private final @NotNull String playerName;
    private final @NotNull String material;
    private final int amount;
    private final double pricePerUnit;

    public OrderCreateRequest(@NotNull UUID playerId, @NotNull String playerName,
                              @NotNull String material, int amount, double pricePerUnit) {
        this.playerId = Objects.requireNonNull(playerId, "playerId must not be null");
        this.playerName = Objects.requireNonNull(playerName, "playerName must not be null");
        this.material = Objects.requireNonNull(material, "material must not be null");
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
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

    /**
     * Validates this request and returns a result.
     *
     * @return a {@link ValidationResult} indicating success or listing errors
     */
    public com.unity.orders.validation.ValidationResult validate() {
        com.unity.orders.validation.ValidationResult.Builder builder =
                com.unity.orders.validation.ValidationResult.builder();

        if (playerName.isBlank()) {
            builder.addError("Player name cannot be blank");
        }
        if (material.isBlank()) {
            builder.addError("Material cannot be blank");
        }
        if (amount <= 0) {
            builder.addError("Amount must be positive");
        }
        if (amount > 2304) {
            builder.addError("Amount cannot exceed 36 stacks (2304 items)");
        }
        if (pricePerUnit < 0) {
            builder.addError("Price per unit cannot be negative");
        }
        if (pricePerUnit > 1_000_000_000) {
            builder.addError("Price per unit exceeds maximum allowed value");
        }

        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderCreateRequest that = (OrderCreateRequest) o;
        return amount == that.amount
                && Double.compare(that.pricePerUnit, pricePerUnit) == 0
                && playerId.equals(that.playerId)
                && playerName.equals(that.playerName)
                && material.equals(that.material);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, playerName, material, amount, pricePerUnit);
    }

    @Override
    public String toString() {
        return "OrderCreateRequest{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", material='" + material + '\'' +
                ", amount=" + amount +
                ", pricePerUnit=" + pricePerUnit +
                '}';
    }
}
