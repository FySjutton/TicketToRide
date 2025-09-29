package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.guis.GuiTools;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static avox.test.ticketToRide.TicketToRide.plugin;

public class TimerManager {
    private final GameHandler handler;
    private Runnable nextAction;
    private int timer;

    public TimerManager(GameHandler handler) {
        this.handler = handler;
        Bukkit.getScheduler().runTaskTimer(plugin, this::count, 0L, 20L);
    }

    public void startTimedAction(int timer, Runnable onceFinished) {
        this.nextAction = onceFinished;
        this.timer = timer;
    }

    private void count() {
        if (handler.playerStateManager.isEmpty()) return;

        boolean allFinished = handler.playerStateManager.values().stream().allMatch(a -> a.finished);
        if (allFinished) {
            handler.playerStateManager.clear();
            nextAction.run();
            return;
        }

        timer--;

        if (timer <= 0) {
            for (GameHandler.PlayerState state : handler.playerStateManager.values()) {
                if (!state.finished) {
                    state.timeOut();
                    state.finished = true;
                }
            }
        }

        String formatted = formatTime(timer);

        for (Player player : handler.game.gamePlayers.keySet()) {
            player.sendActionBar(GuiTools.getYellow("⏳ " + formatted + " left"));
        }

        if (timer == 30 || timer == 10 || timer <= 5 && timer > 0) {
            broadcastReminder("⏳ " + formatted + " left!");
        }
    }

    private String formatTime(int seconds) {
        if (seconds < 0) seconds = 0;
        int mins = seconds / 60;
        int secs = seconds % 60;

        if (mins > 0 && secs > 0) {
            return mins + "m " + secs + "s";
        } else if (mins > 0) {
            return mins + "m 0s";
        } else {
            return secs + "s";
        }
    }

    private void broadcastReminder(String msg) {
        handler.game.broadcast(GuiTools.getYellow(msg));
        for (Player player : handler.game.gamePlayers.keySet()) {
            player.playSound(player.getLocation(), "entity.experience_orb.pickup", 1f, 1f);
        }
    }
}
