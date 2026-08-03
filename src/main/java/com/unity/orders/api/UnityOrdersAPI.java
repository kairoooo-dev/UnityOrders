package com.unity.orders.api;

import com.unity.orders.managers.OrderManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Public API interface for UnityOrders.
 *
 * <p>Other plugins can obtain an instance via
 * {@code Bukkit.getServicesManager().getRegistration(UnityOrdersAPI.class)}
 * or by casting {@code UnityOrders.getInstance().getAPI()}.</p>
 */
public interface UnityOrdersAPI {

    /**
     * Creates a buy order on behalf of a player.
     *
     * @param player   the buyer
     * @param material the material to buy
     * @param amount   the quantity
     * @param pricePerUnit the price per unit
     * @return the result of the operation
     */
    @NotNull
    APIResult createOrder(@NotNull Player player, @NotNull Material material, int amount, double pricePerUnit);

    /**
     * Fulfills (partially or fully) a buy order.
     *
     * @param orderId the order ID
     * @param seller  the player fulfilling the order
     * @param quantity the quantity to fulfill
     * @return the result
     */
    @NotNull
    APIResult fulfillOrder(long orderId, @NotNull Player seller, int quantity);

    /**
     * Cancels a buy order.
     *
     * @param orderId the order ID
     * @param player  the player cancelling (must be buyer or admin)
     * @return the result
     */
    @NotNull
    APIResult cancelOrder(long orderId, @NotNull Player player);

    /**
     * Gets all orders for a player.
     *
     * @param playerUUID the player UUID
     * @return an unmodifiable list of orders
     */
    @NotNull
    List<OrderManager.BuyOrder> getOrders(@NotNull UUID playerUUID);

    /**
     * Gets all open orders for a specific material.
     *
     * @param material the material
     * @return an unmodifiable list of orders
     */
    @NotNull
    List<OrderManager.BuyOrder> getOrdersForMaterial(@NotNull Material material);

    /**
     * Gets all active orders on the marketplace.
     *
     * @return an unmodifiable list of all orders
     */
    @NotNull
    List<OrderManager.BuyOrder> getAllOrders();

    /**
     * Finds a specific order by ID.
     *
     * @param orderId the order ID
     * @return the order, or null if not found
     */
    @Nullable
    OrderManager.BuyOrder findOrder(long orderId);
}
