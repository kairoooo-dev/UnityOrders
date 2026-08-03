package com.unity.orders.gui;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents an active GUI session for a player.
 *
 * <p>Tracks the GUI type, current page, and the player's UUID.
 * Used by {@link com.unity.orders.listeners.GuiListener} to route
 * inventory click events to the correct handler.</p>
 */
public final class GuiSession {

    private final UUID playerUUID;
    private final GuiType guiType;
    private int page;

    public GuiSession(@NotNull UUID playerUUID, @NotNull GuiType guiType, int page) {
        this.playerUUID = playerUUID;
        this.guiType = guiType;
        this.page = page;
    }

    @NotNull
    public UUID getPlayerUUID() { return playerUUID; }

    @NotNull
    public GuiType getGuiType() { return guiType; }

    public int getPage() { return page; }

    public void setPage(int page) { this.page = page; }
}
