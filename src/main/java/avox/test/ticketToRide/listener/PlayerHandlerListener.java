package avox.test.ticketToRide.listener;

import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.GameManager;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

import static avox.test.ticketToRide.TicketToRide.playerStateManager;

public class PlayerHandlerListener implements Listener {
    private final JavaPlugin plugin;
    private final HashMap<Player, Long> pendingLeaves = new HashMap<>();

    public PlayerHandlerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!GameManager.activePlayers.contains(player)) {
            playerStateManager.restorePlayer(player);
        } else {
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
        }

        if (pendingLeaves.containsKey(player)) {
            pendingLeaves.remove(player);
            Game game = GameManager.getGameByUser(player);
            if (game != null) {
                game.broadcast("§a" + player.getName() + " rejoined!");
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (GameManager.activePlayers.contains(player)) {
            event.setCancelled(true);
            player.setHealth(player.getMaxHealth());
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (GameManager.activePlayers.contains(player)) {
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (GameManager.activePlayers.contains(player)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location spawn = player.getWorld().getSpawnLocation();
                player.teleport(spawn);
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
            }, 1L);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (GameManager.activePlayers.contains(player)) {
            Location spawn = player.getWorld().getSpawnLocation();
            event.setRespawnLocation(spawn);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
            }, 1L);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (GameManager.activePlayers.contains(player)) {
            if (player.getLocation().getY() < 0) {
                Location safeLocation = player.getWorld().getSpawnLocation();
                player.teleport(safeLocation);
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (GameManager.activePlayers.contains(event.getPlayer())) {
            Game game = GameManager.getGameByUser(event.getPlayer());
            if (game != null) {
                if (game.gamePlayers.size() == 1) {
                    game.leaveGame(event.getPlayer());
                } else {
                    pendingLeaves.put(event.getPlayer(), System.currentTimeMillis());
                    game.broadcast("§e" + event.getPlayer().getName() + "§c left the server!\n§7They have one minute to rejoin before being kicked from the game!");
                }
            }
        }
    }

    @EventHandler
    public void onTick(ServerTickEndEvent event) {
        ArrayList<Player> playersToRemove = new ArrayList<>();
        pendingLeaves.forEach((player, leaveTime) -> {
            if (leaveTime + 60000 <= System.currentTimeMillis()) {
                playersToRemove.add(player);
                Game game = GameManager.getGameByUser(player);
                if (game != null) {
                    game.leaveGame(player);
                }
            }
        });
        playersToRemove.forEach(pendingLeaves::remove);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!GameManager.activePlayers.contains(player)) return;
        event.setCancelled(true);
    }
}
