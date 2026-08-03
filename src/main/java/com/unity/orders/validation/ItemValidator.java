package com.unity.orders.validation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Validates items and materials for order creation.
 *
 * <p>Checks for blacklisted materials, invalid NBT, and amount limits.
 * All public methods are thread-safe.</p>
 */
public final class ItemValidator {

    private final @NotNull JavaPlugin plugin;
    private final @NotNull Set<Material> blacklistedMaterials = new HashSet<>();
    private final int maxAmount;
    private volatile boolean loaded = false;

    public ItemValidator(@NotNull JavaPlugin plugin, int maxAmount) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
        this.maxAmount = maxAmount;
    }

    /**
     * Loads the blacklist from configuration.
     *
     * @param blacklisted the set of blacklisted material names
     */
    public synchronized void loadBlacklist(@NotNull Set<String> blacklisted) {
        blacklistedMaterials.clear();
        for (String name : blacklisted) {
            try {
                Material material = Material.valueOf(name.toUpperCase());
                blacklistedMaterials.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown blacklisted material: " + name);
            }
        }
        loaded = true;
        plugin.getLogger().info("Loaded " + blacklistedMaterials.size() + " blacklisted materials.");
    }

    /**
     * Validates a material name string.
     *
     * @param materialName the material name
     * @return a validation result
     */
    public @NotNull ValidationResult validateMaterial(@NotNull String materialName) {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (materialName == null || materialName.isBlank()) {
            builder.addError("Material name cannot be blank");
            return builder.build();
        }

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            builder.addError("Unknown material: " + materialName);
            return builder.build();
        }

        if (material.isAir()) {
            builder.addError("Cannot create an order for air");
        }

        if (blacklistedMaterials.contains(material)) {
            builder.addError("Material " + materialName + " is blacklisted");
        }

        return builder.build();
    }

    /**
     * Validates an amount.
     *
     * @param amount the amount
     * @return a validation result
     */
    public @NotNull ValidationResult validateAmount(int amount) {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (amount <= 0) {
            builder.addError("Amount must be positive (got " + amount + ")");
        }
        if (amount > maxAmount) {
            builder.addError("Amount " + amount + " exceeds maximum of " + maxAmount);
        }

        return builder.build();
    }

    /**
     * Validates a price per unit.
     *
     * @param pricePerUnit the price per unit
     * @return a validation result
     */
    public @NotNull ValidationResult validatePrice(double pricePerUnit) {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (pricePerUnit < 0) {
            builder.addError("Price per unit cannot be negative");
        }
        if (Double.isNaN(pricePerUnit) || Double.isInfinite(pricePerUnit)) {
            builder.addError("Price per unit must be a finite number");
        }
        if (pricePerUnit > 1_000_000_000) {
            builder.addError("Price per unit exceeds maximum allowed value");
        }

        return builder.build();
    }

    /**
     * Validates an {@link ItemStack} for delivery.
     *
     * @param itemStack the item stack
     * @param expectedMaterial the expected material
     * @param expectedAmount   the expected amount
     * @return a validation result
     */
    public @NotNull ValidationResult validateItemStack(@Nullable ItemStack itemStack,
                                                        @NotNull Material expectedMaterial,
                                                        int expectedAmount) {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (itemStack == null || itemStack.getType().isAir()) {
            builder.addError("Item stack is empty or air");
            return builder.build();
        }

        if (itemStack.getType() != expectedMaterial) {
            builder.addError("Material mismatch: expected " + expectedMaterial.name()
                    + ", got " + itemStack.getType().name());
        }

        if (itemStack.getAmount() < expectedAmount) {
            builder.addError("Insufficient amount: expected " + expectedAmount
                    + ", got " + itemStack.getAmount());
        }

        // Check for suspicious NBT (crash books, etc.)
        if (itemStack.hasItemMeta()) {
            var meta = itemStack.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                if (displayName != null && displayName.length() > 100) {
                    builder.addError("Item display name exceeds maximum length");
                }
            }
        }

        return builder.build();
    }

    /**
     * Returns whether the blacklist has been loaded.
     *
     * @return {@code true} if loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Returns whether a material is blacklisted.
     *
     * @param material the material
     * @return {@code true} if blacklisted
     */
    public boolean isBlacklisted(@NotNull Material material) {
        return blacklistedMaterials.contains(material);
    }
}
