package com.unity.orders.managers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable result object returned by {@link OrderManager} operations.
 *
 * <p>Carries success/failure status, a human-readable message, and
 * an optional reference to the affected {@link OrderManager.BuyOrder}.</p>
 */
public final class OrderResult {

    private final boolean success;
    private final String message;
    private final OrderManager.BuyOrder order;

    private OrderResult(boolean success, @NotNull String message, @Nullable OrderManager.BuyOrder order) {
        this.success = success;
        this.message = message;
        this.order = order;
    }

    @NotNull
    public static OrderResult success(@NotNull String message) {
        return new OrderResult(true, message, null);
    }

    @NotNull
    public static OrderResult success(@NotNull String message, @Nullable OrderManager.BuyOrder order) {
        return new OrderResult(true, message, order);
    }

    @NotNull
    public static OrderResult failure(@NotNull String message) {
        return new OrderResult(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }
    @NotNull public String getMessage() { return message; }
    @Nullable public OrderManager.BuyOrder getOrder() { return order; }
}
