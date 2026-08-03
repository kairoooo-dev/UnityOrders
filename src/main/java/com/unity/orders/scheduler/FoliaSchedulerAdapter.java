package com.unity.orders.scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * {@link SchedulerAdapter} implementation for Folia servers.
 *
 * <p>Uses Folia's region scheduler for location-bound tasks, entity scheduler
 * for entity-bound tasks, async scheduler for asynchronous work, and the
 * global region scheduler for general synchronous tasks.</p>
 *
 * <p>This class uses reflection to access Folia scheduler APIs, allowing the
 * plugin to compile against standard Paper while still supporting Folia at
 * runtime.</p>
 */
public final class FoliaSchedulerAdapter implements SchedulerAdapter {

    private final @NotNull Plugin plugin;
    private final @NotNull Set<Object> taskHandles = ConcurrentHashMap.newKeySet();

    // Folia scheduler instances (obtained via reflection)
    private final @NotNull Object asyncScheduler;
    private final @NotNull Object globalRegionScheduler;
    private final @NotNull Object regionScheduler;
    private final @NotNull Object entityScheduler;

    public FoliaSchedulerAdapter(@NotNull Plugin plugin) {
        this.plugin = plugin;
        var server = plugin.getServer();

        this.asyncScheduler = invokeGetter(server, "getAsyncScheduler");
        this.globalRegionScheduler = invokeGetter(server, "getGlobalRegionScheduler");
        this.regionScheduler = invokeGetter(server, "getRegionScheduler");
        this.entityScheduler = invokeGetter(server, "getEntityScheduler");
    }

    @Override
    public void runSync(@NotNull Runnable task) {
        // On Folia, use the global region scheduler for non-location-bound sync tasks
        Object handle = invokeMethod(globalRegionScheduler, "run", task.getClass(),
                (runnable) -> invokeMethodWithArgs(globalRegionScheduler, "run",
                        new Class[]{Plugin.class, Runnable.class},
                        new Object[]{plugin, task}));
        // Folia's run() returns a ScheduledTask; track it
        trackHandle(handle);
    }

    @Override
    public void runAsync(@NotNull Runnable task) {
        Object handle = invokeMethodWithArgs(asyncScheduler, "runNow",
                new Class[]{Plugin.class, Runnable.class},
                new Object[]{plugin, task});
        trackHandle(handle);
    }

    @Override
    public void runSyncDelayed(@NotNull Runnable task, long delayTicks) {
        long delayMs = ticksToMillis(delayTicks);
        Object handle = invokeMethodWithArgs(globalRegionScheduler, "runDelayed",
                new Class[]{Plugin.class, Runnable.class, long.class, TimeUnit.class},
                new Object[]{plugin, task, delayMs, TimeUnit.MILLISECONDS});
        trackHandle(handle);
    }

    @Override
    public void runAsyncDelayed(@NotNull Runnable task, long delayTicks) {
        long delayMs = ticksToMillis(delayTicks);
        Object handle = invokeMethodWithArgs(asyncScheduler, "runDelayed",
                new Class[]{Plugin.class, Runnable.class, long.class, TimeUnit.class},
                new Object[]{plugin, task, delayMs, TimeUnit.MILLISECONDS});
        trackHandle(handle);
    }

    @Override
    public void runSyncRepeating(@NotNull Runnable task, long delayTicks, long periodTicks) {
        long delayMs = ticksToMillis(delayTicks);
        long periodMs = ticksToMillis(periodTicks);
        Object handle = invokeMethodWithArgs(globalRegionScheduler, "runAtFixedRate",
                new Class[]{Plugin.class, Runnable.class, long.class, long.class, TimeUnit.class},
                new Object[]{plugin, task, delayMs, periodMs, TimeUnit.MILLISECONDS});
        trackHandle(handle);
    }

    @Override
    public void runAsyncRepeating(@NotNull Runnable task, long delayTicks, long periodTicks) {
        long delayMs = ticksToMillis(delayTicks);
        long periodMs = ticksToMillis(periodTicks);
        Object handle = invokeMethodWithArgs(asyncScheduler, "runAtFixedRate",
                new Class[]{Plugin.class, Runnable.class, long.class, long.class, TimeUnit.class},
                new Object[]{plugin, task, delayMs, periodMs, TimeUnit.MILLISECONDS});
        trackHandle(handle);
    }

    @Override
    public void runAtLocation(@NotNull Location location, @NotNull Runnable task) {
        Object handle = invokeMethodWithArgs(regionScheduler, "run",
                new Class[]{Location.class, Plugin.class, Runnable.class},
                new Object[]{location, plugin, task});
        trackHandle(handle);
    }

    @Override
    public void runAtEntity(@NotNull Entity entity, @NotNull Runnable task) {
        Object handle = invokeMethodWithArgs(entityScheduler, "run",
                new Class[]{Entity.class, Plugin.class, Runnable.class},
                new Object[]{entity, plugin, task});
        trackHandle(handle);
    }

    @Override
    public void runAtEntityDelayed(@NotNull Entity entity, @NotNull Runnable task, long delayTicks) {
        long delayMs = ticksToMillis(delayTicks);
        Object handle = invokeMethodWithArgs(entityScheduler, "runDelayed",
                new Class[]{Entity.class, Plugin.class, Runnable.class, long.class, TimeUnit.class},
                new Object[]{entity, plugin, task, delayMs, TimeUnit.MILLISECONDS});
        trackHandle(handle);
    }

    @Override
    public void cancelAll() {
        for (Object handle : new HashSet<>(taskHandles)) {
            if (handle != null) {
                invokeMethod(handle, "cancel");
            }
        }
        taskHandles.clear();
    }

    @Override
    public boolean isFolia() {
        return true;
    }

    // ── Reflection helpers ───────────────────────────────────────────

    private void trackHandle(Object handle) {
        if (handle != null) {
            taskHandles.add(handle);
        }
    }

    private static long ticksToMillis(long ticks) {
        return Math.max(0, ticks) * 50L;
    }

    private static @NotNull Object invokeGetter(@NotNull Object target, @NotNull String getterName) {
        try {
            var method = target.getClass().getMethod(getterName);
            return method.invoke(target);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke " + getterName + " on " + target.getClass().getName(), e);
        }
    }

    private static @Nullable Object invokeMethod(@NotNull Object target, @NotNull String methodName) {
        try {
            var method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke " + methodName + " on " + target.getClass().getName(), e);
        }
    }

    private static @Nullable Object invokeMethod(@NotNull Object target, @NotNull String methodName,
                                                  @SuppressWarnings("unused") Class<?> unused,
                                                  @NotNull java.util.function.Function<Object, Object> fn) {
        return fn.apply(null);
    }

    private static @Nullable Object invokeMethodWithArgs(@NotNull Object target, @NotNull String methodName,
                                                          @NotNull Class<?>[] paramTypes,
                                                          @NotNull Object[] args) {
        try {
            var method = target.getClass().getMethod(methodName, paramTypes);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to invoke " + methodName + " on " + target.getClass().getName(), e);
        }
    }
}
