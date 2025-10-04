package avox.test.ticketToRide.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Waiter {
    private final JavaPlugin plugin;
    private long accumulatedTicks = 0L;

    public Waiter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Waiter waitSeconds(int seconds, Runnable after) {
        long ticks = seconds * 20L;
        accumulatedTicks += ticks;
        Bukkit.getScheduler().runTaskLater(plugin, after, accumulatedTicks);
        return this;
    }
}
