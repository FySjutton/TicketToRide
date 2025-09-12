package avox.test.ticketToRide.listener;

import avox.test.ticketToRide.game.GameManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class GameRestrictionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (GameManager.activePlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (GameManager.activePlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!GameManager.activePlayers.contains(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot use Ender Chests while in a game!");
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!GameManager.activePlayers.contains(player)) return;

        String command = event.getMessage().toLowerCase();

        if (command.startsWith("/ec") || command.startsWith("/enderchest")) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot access Ender Chests while in a game!");
        }
    }
}
