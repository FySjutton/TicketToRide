package avox.test.ticketToRide.game;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.function.Consumer;

public class Countdown extends BukkitRunnable {
    private final JavaPlugin plugin;
    private int seconds;
    private final Runnable onFinish;
    private final Consumer<Integer> countCallback;

    public Countdown(JavaPlugin plugin, int seconds, Consumer<Integer> countCallback, Runnable onFinish) {
        this.plugin = plugin;
        this.seconds = seconds;
        this.onFinish = onFinish;
        this.countCallback = countCallback;
    }

    @Override
    public void run() {
        if (seconds <= 0) {
            cancel();
            if (onFinish != null) {
                onFinish.run();
            }
            return;
        }
        countCallback.accept(seconds);
        seconds--;
    }

    public void start() {
        this.runTaskTimer(plugin, 0L, 20L);
    }

    public static NamedTextColor getColor(int seconds) {
        if (List.of(1, 2, 3).contains(seconds)) {
            return NamedTextColor.RED;
        }
        return NamedTextColor.YELLOW;
    }
}
