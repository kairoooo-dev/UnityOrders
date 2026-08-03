package com.unity.orders.api;

import com.unity.orders.UnityOrders;
import com.unity.orders.managers.OrderManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Concrete implementation of {@link UnityOrdersAPI}.
 *
 * <p>Delegates to {@link OrderManager} for all business logic.
 * This class is the bridge between the public API surface and
 * the internal manager layer.</p>
 */
public final class APIImplementation implements UnityOrdersAPI {

    private final UnityOrders plugin;

    public APIImplementation(@NotNull UnityOrders plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public APIResult createOrder(@NotNull Player player, @NotNull Material material, int amount, double pricePerUnit) {
        OrderResult result = plugin.getOrderManager().createOrder(player, material, amount, pricePerUnit);
        return toAPIResult(result);
    }

    @Override
    @NotNull
    public APIResult fulfillOrder(long orderId, @NotNull Player seller, int quantity) {
        OrderResult result = plugin.getOrderManager().fulfillOrder(orderId, seller, quantity);
        return toAPIResult(result);
    }

    @Override
    @NotNull
    public APIResult cancelOrder(long orderId, @NotNull Player player) {
        OrderResult result = plugin.getOrderManager().cancelOrder(orderId, player);
        return toAPIResult(result);
    }

    @Override
    @NotNull
    public List<OrderManager.BuyOrder> getOrders(@NotNull UUID playerUUID) {
        return plugin.getOrderManager().getOrders(playerUUID);
    }

    @Override
    @NotNull
    public List<OrderManager.BuyOrder> getOrdersForMaterial(@NotNull Material material) {
        return plugin.getOrderManager().getOrdersForMaterial(material);
    }

    @Override
    @NotNull
    public List<OrderManager.BuyOrder> getAllOrders() {
        return plugin.getOrderManager().getAllOrders();
    }

    @Override
    @Nullable
    public OrderManager.BuyOrder findOrder(long orderId) {
        return plugin.getOrderManager().findOrder(orderId);
    }

    @NotNull
    private static APIResult toAPIResult(@NotNull OrderResult result) {
        if (result.isSuccess()) {
            return APIResult.success(result.getMessage(), result.getOrder());
        } else {
            return APIResult.failure(result.getMessage());
        }
    }
}
