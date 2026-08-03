package com.unity.orders.scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link SchedulerAdapter} implementation for standard Bukkit/Paper/Spigot servers.
 *
 * <p>Delegates to {@link BukkitScheduler} for all task scheduling. All methods
 * are thread-safe — task IDs are tracked in a concurrent set for cancellation.</p>
 */
public final class BukkitSchedulerAdapter implements SchedulerAdapter {

    private final @NotNull Plugin plugin;
    private final @NotNull BukkitScheduler scheduler;
    private final @NotNull Set<Integer> taskIds = ConcurrentHashMap.newKeySet();

    public BukkitSchedulerAdapter(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public void runSync(@NotNull Runnable task) {
        int id = scheduler.runTask(plugin, task).getTaskId();
        taskIds.add(id);
    }

    @Override
    public void runAsync(@NotNull Runnable task) {
        int id = scheduler.runTaskAsynchronously(plugin, task).getTaskId();
        taskIds.add(id);
    }

    @Override
    public void runSyncDelayed(@NotNull Runnable task, long delayTicks) {
        int id = scheduler.runTaskLater(plugin, task, delayTicks).getTaskId();
        taskIds.add(id);
    }

    @Override
    public void runAsyncDelayed(@NotNull Runnable task, long delayTicks) {
        int id = scheduler.runTaskLaterAsynchronously(plugin, task, delayTicks).getTaskId();
        taskIds.add(id);
    }

    @Override
    public void runSyncRepeating(@NotNull Runnable task, long delayTicks, long periodTicks) {
        int id = scheduler.runTaskTimer(plugin, task, delayTicks, periodTicks).getTaskId();
        taskIds.add(id);
    }

    @Override
    public void runAsyncRepeating(@NotNull Runnable task, long delayTicks, long periodTicks) {
        int id = scheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks).getTaskId();
        taskIds.add(id);
    }

    @Override
    public void runAtLocation(@NotNull Location location, @NotNull Runnable task) {
        // On Bukkit, all sync tasks run on the main thread — location is irrelevant
        runSync(task);
    }

    @Override
    public void runAtEntity(@NotNull Entity entity, @NotNull Runnable task) {
        runSync(task);
    }

    @Override
    public void runAtEntityDelayed(@NotNull Entity entity, @NotNull Runnable task, long delayTicks) {
        runSyncDelayed(task, delayTicks);
    }

    @Override
    public void cancelAll() {
        for (int id : new HashSet<>(taskIds)) {
            scheduler.cancelTask(id);
        }
        taskIds.clear();
    }

    @Override
    public boolean isFolia() {
        return false;
    }
}
