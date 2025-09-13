package avox.test.ticketToRide.listener;

import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.GameMap;
import avox.test.ticketToRide.game.Route;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static avox.test.ticketToRide.game.GameManager.activePlayers;
import static avox.test.ticketToRide.game.GameManager.getGameByUser;

public class ClickListener implements Listener {
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
            if (event.getAttacked() instanceof ItemFrame) {
                Game game = getGameByUser(event.getPlayer());
                if (game == null) return;

                raytraceClick(event.getPlayer(), game);
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
        Game game = getGameByUser(player);
        if (game == null) return;

        raytraceClick(player, game);
    }

    private void raytraceClick(Player player, Game game) {
        RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                20,
                FluidCollisionMode.NEVER,
                true
        );

        if (result == null) return;
        result.getHitPosition();
        Vector hit = result.getHitPosition();

        checkTile(player, hit, game);
    }

    private void checkTile(Player player, Vector hit, Game game) {
        Location baseLocation = game.gameMap.getStartLocation(game.arena);

        double pixelX = (hit.getX() - baseLocation.getX()) * 128;
        double pixelY = (hit.getZ() - baseLocation.getZ()) * 128;

        if (pixelX < 0 || pixelY < 0 || pixelX > game.gameMap.width || pixelY > game.gameMap.height) return;
        for (Route route : game.gameMap.routes) {
            for (Route.Tile tile : route.tiles) {
                if (tile.x < pixelX && tile.y < pixelY && tile.x + tile.widthBounding > pixelX && tile.y + tile.heightBounding > pixelY) {
                    int relativeX = (int) (pixelX - tile.x);
                    int relativeY = (int) (pixelY - tile.y);

                    boolean hitTile = game.gameMap.tileMaps.get(tile.rotation).hit(relativeX, relativeY);
                    if (hitTile) {
                        player.sendMessage("§dClicked tile!");
                    }
                }
            }
        }
    }
}
