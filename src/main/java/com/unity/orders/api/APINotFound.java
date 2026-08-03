package com.unity.orders.api;

/**
 * Exception thrown when the UnityOrders API is not available.
 *
 * <p>This typically means UnityOrders is not installed or failed to
 * enable. Callers should catch this and degrade gracefully.</p>
 */
public final class APINotFound extends RuntimeException {

    public APINotFound() {
        super("UnityOrders API is not available. Is the plugin installed and enabled?");
    }

    public APINotFound(@NotNull String message) {
        super(message);
    }

    public APINotFound(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
