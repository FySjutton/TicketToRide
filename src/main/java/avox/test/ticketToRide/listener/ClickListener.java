package avox.test.ticketToRide.listener;

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import static avox.test.ticketToRide.game.GameManager.activePlayers;

public class ClickListener implements Listener {
    private final Location baseLocation;
    private final int gridSize = 8;
    private final int tilePixels = 128;
    private final double tileSize = 1.0;

    public ClickListener(Location baseLocation) {
        this.baseLocation = baseLocation;
    }

    @EventHandler
    public void itemFrameChanged(PlayerItemFrameChangeEvent event) {
        if (activePlayers.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void entityAttack(PrePlayerAttackEntityEvent event) {
        if (activePlayers.contains(event.getPlayer())) {
            event.setCancelled(true);
            if (event.getAttacked() instanceof ItemFrame frame) {
                sendPixelCoords(event.getPlayer(), frame.getLocation().toVector());
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            if (event.getDamager() instanceof Player) {
                Player player = ((Player) event.getDamager()).getPlayer();
                if (activePlayers.contains(player)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityBreak(HangingBreakByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            if (event.getRemover() instanceof Player) {
                Player player = ((Player) event.getRemover()).getPlayer();
                if (activePlayers.contains(player)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent event) {
        if (!activePlayers.contains(event.getPlayer())) return;
        if (!event.getAction().toString().contains("LEFT_CLICK")) return;

        Player player = event.getPlayer();

        RayTraceResult result = player.getWorld().rayTrace(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                20,
                FluidCollisionMode.NEVER,
                true,
                0.1,
                entity -> false
        );

        if (result == null || result.getHitPosition() == null) return;
        Vector hit = result.getHitPosition();

        double relX = hit.getX() - baseLocation.getX();
        double relZ = hit.getZ() - baseLocation.getZ();

        if (relX < 0 || relZ < 0 || relX > gridSize * tileSize || relZ > gridSize * tileSize) return;

        sendPixelCoords(player, hit);
    }

    private void sendPixelCoords(Player player, Vector hit) {
        double relX = hit.getX() - baseLocation.getX();
        double relZ = hit.getZ() - baseLocation.getZ();

        int pixelX = (int) ((relX / (gridSize * tileSize)) * (tilePixels * gridSize));
        int pixelY = (int) ((relZ / (gridSize * tileSize)) * (tilePixels * gridSize));

        int tileX = pixelX / tilePixels;
        int tileY = pixelY / tilePixels;

        player.sendMessage(ChatColor.GREEN + "Klickade på pixel (" + pixelX + ", " + pixelY +
                ") på tile (" + tileX + ", " + tileY + ")");
    }
}
