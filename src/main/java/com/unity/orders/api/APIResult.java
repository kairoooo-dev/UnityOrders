package com.unity.orders.api;

import com.unity.orders.managers.OrderManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable result object returned by {@link UnityOrdersAPI} operations.
 *
 * <p>Carries success/failure status, a message, and an optional
 * reference to the affected {@link OrderManager.BuyOrder}.</p>
 */
public final class APIResult {

    private final boolean success;
    private final String message;
    private final OrderManager.BuyOrder order;

    private APIResult(boolean success, @NotNull String message, @Nullable OrderManager.BuyOrder order) {
        this.success = success;
        this.message = message;
        this.order = order;
    }

    @NotNull
    public static APIResult success(@NotNull String message) {
        return new APIResult(true, message, null);
    }

    @NotNull
    public static APIResult success(@NotNull String message, @Nullable OrderManager.BuyOrder order) {
        return new APIResult(true, message, order);
    }

    @NotNull
    public static APIResult failure(@NotNull String message) {
        return new APIResult(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }
    @NotNull public String getMessage() { return message; }
    @Nullable public OrderManager.BuyOrder getOrder() { return order; }
}
