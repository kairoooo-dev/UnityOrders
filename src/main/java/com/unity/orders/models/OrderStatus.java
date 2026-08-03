package com.unity.orders.models;

/**
 * Represents the lifecycle state of an order.
 *
 * <p>State transitions:</p>
 * <pre>
 * PENDING -> ACTIVE -> DELIVERED
 * PENDING -> CANCELLED
 * ACTIVE -> CANCELLED
 * ACTIVE -> EXPIRED
 * </pre>
 */
public enum OrderStatus {

    /**
     * The order has been created but not yet activated.
     */
    PENDING,

    /**
     * The order is active and available for fulfillment.
     */
    ACTIVE,

    /**
     * The order has been successfully delivered.
     */
    DELIVERED,

    /**
     * The order was cancelled by the player or an administrator.
     */
    CANCELLED,

    /**
     * The order expired before being fulfilled.
     */
    EXPIRED;

    /**
     * Checks whether this status represents a terminal state (no further transitions).
     *
     * @return {@code true} if the status is terminal
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED || this == EXPIRED;
    }

    /**
     * Checks whether a transition from this status to the target is valid.
     *
     * @param target the target status
     * @return {@code true} if the transition is allowed
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (target == null) return false;
        if (isTerminal()) return false;

        return switch (this) {
            case PENDING -> target == ACTIVE || target == CANCELLED;
            case ACTIVE -> target == DELIVERED || target == CANCELLED || target == EXPIRED;
            default -> false;
        };
    }
}
