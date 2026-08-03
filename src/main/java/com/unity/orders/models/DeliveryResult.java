package com.unity.orders.models;

import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable result object representing the outcome of a delivery attempt.
 *
 * <p>Use {@link #success(Order)} to create a successful result and
 * {@link #failure(String)} to create a failed result.</p>
 */
public final class DeliveryResult {

    private final boolean success;
    private final @Nullable Order order;
    private final @Nullable String errorMessage;

    private DeliveryResult(boolean success, @Nullable Order order, @Nullable String errorMessage) {
        this.success = success;
        this.order = order;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful delivery result.
     *
     * @param order the delivered order
     * @return a successful result
     */
    public static @NotNull DeliveryResult success(@NotNull Order order) {
        return new DeliveryResult(true, Objects.requireNonNull(order, "order must not be null"), null);
    }

    /**
     * Creates a failed delivery result.
     *
     * @param errorMessage a human-readable error message
     * @return a failed result
     */
    public static @NotNull DeliveryResult failure(@NotNull String errorMessage) {
        return new DeliveryResult(false, null, Objects.requireNonNull(errorMessage, "errorMessage must not be null"));
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public @NotNull Optional<Order> getOrder() {
        return Optional.ofNullable(order);
    }

    public @NotNull Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryResult that = (DeliveryResult) o;
        return success == that.success
                && Objects.equals(order, that.order)
                && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, order, errorMessage);
    }

    @Override
    public String toString() {
        return "DeliveryResult{" +
                "success=" + success +
                ", order=" + order +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
