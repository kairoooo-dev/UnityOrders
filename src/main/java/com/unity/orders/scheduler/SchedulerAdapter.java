package com.unity.orders.scheduler;

import java.util.concurrent.TimeUnit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstraction layer over server schedulers, supporting both Bukkit and Folia.
 *
 * <p>Implementations must guarantee thread-safety for all methods. On Folia,
 * region-based scheduling is used for location/entity tasks. On Bukkit,
 * the standard synchronous/asynchronous scheduler is used.</p>
 */
public interface SchedulerAdapter {

    /**
     * Runs a task on the main server thread (or global region on Folia).
     *
     * @param task the task to run
     */
    void runSync(@NotNull Runnable task);

    /**
     * Runs a task asynchronously.
     *
     * @param task the task to run
     */
    void runAsync(@NotNull Runnable task);

    /**
     * Runs a task on the main server thread after a delay.
     *
     * @param task      the task to run
     * @param delayTicks the delay in ticks (20 ticks = 1 second)
     */
    void runSyncDelayed(@NotNull Runnable task, long delayTicks);

    /**
     * Runs a task asynchronously after a delay.
     *
     * @param task      the task to run
     * @param delayTicks the delay in ticks
     */
    void runAsyncDelayed(@NotNull Runnable task, long delayTicks);

    /**
     * Runs a repeating task on the main server thread.
     *
     * @param task             the task to run
     * @param delayTicks       the initial delay in ticks
     * @param periodTicks      the period between executions in ticks
     */
    void runSyncRepeating(@NotNull Runnable task, long delayTicks, long periodTicks);

    /**
     * Runs a repeating task asynchronously.
     *
     * @param task             the task to run
     * @param delayTicks       the initial delay in ticks
     * @param periodTicks      the period between executions in ticks
     */
    void runAsyncRepeating(@NotNull Runnable task, long delayTicks, long periodTicks);

    /**
     * Runs a task at the region containing the given location.
     * On Bukkit, this is equivalent to {@link #runSync(Runnable)}.
     *
     * @param location the location
     * @param task     the task to run
     */
    void runAtLocation(@NotNull Location location, @NotNull Runnable task);

    /**
     * Runs a task on the entity's region.
     * On Bukkit, this is equivalent to {@link #runSync(Runnable)}.
     *
     * @param entity the entity
     * @param task   the task to run
     */
    void runAtEntity(@NotNull Entity entity, @NotNull Runnable task);

    /**
     * Runs a delayed task on the entity's region.
     *
     * @param entity      the entity
     * @param task        the task to run
     * @param delayTicks  the delay in ticks
     */
    void runAtEntityDelayed(@NotNull Entity entity, @NotNull Runnable task, long delayTicks);

    /**
     * Cancels all tasks managed by this scheduler.
     */
    void cancelAll();

    /**
     * Returns whether this adapter is running on a Folia server.
     *
     * @return {@code true} if running on Folia
     */
    boolean isFolia();
}
