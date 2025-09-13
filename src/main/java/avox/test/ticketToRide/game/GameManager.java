package avox.test.ticketToRide.game;

import avox.test.ticketToRide.config.ArenaManager;
import avox.test.ticketToRide.config.FileConverter;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.renderer.MapSummoner;
import avox.test.ticketToRide.renderer.TrainRenderer;
import avox.test.ticketToRide.utils.BillboardManager;
import avox.test.ticketToRide.config.MapManager;
import avox.test.ticketToRide.utils.board.MarkerManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static avox.test.ticketToRide.TicketToRide.playerStateManager;


public class GameManager {
    public static ArrayList<Player> activePlayers = new ArrayList<>();
    public static ArrayList<Game> activeGames = new ArrayList<>();

    public static boolean createGame(JavaPlugin plugin, Player gameOwner, String map, String arena) {
        try {
            GameMap gameMap = FileConverter.convertFileToMap(new File(plugin.getDataFolder() + "/maps", map));
            Arena gameArena = FileConverter.convertFileToArena(arena, new File(plugin.getDataFolder() + "/arenas", arena));
            if (gameMap == null || gameArena == null) {
                return false;
            }

            new MapSummoner().generateAndDisplay(gameArena.world, gameMap.map, gameArena.mapStartPosition, gameMap.tilesX, gameMap.tilesY);
//        new TrainRenderer().spawnSmallTrainCar(gameWorld, base);

            new BillboardManager().summonBillboards(gameArena);


            // Testing
            TrainRenderer renderer = new TrainRenderer();
            for (Route route : gameMap.routes) {
                for (Route.Tile tile : route.tiles) {
                    renderer.spawnSmallTrainCar(gameArena, gameMap, tile);
                }
            }

            activeGames.add(new Game(gameOwner, gameArena, gameMap));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void startGame(Game game) {
        game.started = true;

        MarkerManager markerManager = new MarkerManager();
        for (GamePlayer player : game.players) {
            player.player.getInventory().setBoots(setLeatherColor(new ItemStack(Material.LEATHER_BOOTS), player.markerData.getLeft()));
            player.player.getInventory().setLeggings(setLeatherColor(new ItemStack(Material.LEATHER_LEGGINGS), player.markerData.getLeft()));
            player.player.getInventory().setChestplate(setLeatherColor(new ItemStack(Material.LEATHER_CHESTPLATE), player.markerData.getLeft()));
            player.player.getInventory().setHelmet(setLeatherColor(new ItemStack(Material.LEATHER_HELMET), player.markerData.getLeft()));

            player.marker = markerManager.spawnMarker(game, player.markerData.getRight());
            player.points = 0;
            markerManager.reposition(game, player, player.points);
        }
    }

    public static void deleteGame(Game game) {
        activeGames.remove(game);
        for (Player player : game.arena.world.getPlayers()) {
            activePlayers.remove(player);
            playerStateManager.restorePlayer(player);
        }
        ArenaManager.unloadAndDeleteArenaWorld(game.arena.mapID);
    }

    public static void changeOwner(Game game, Player newOwner) {
        game.gameOwner = newOwner;
    }

    public static Game getGameByOwner(Player owner) {
        List<Game> games = activeGames.stream().filter(game -> game.gameOwner == owner).toList();
        if (games.isEmpty()) return null;
        return games.getFirst();
    }

    public static Game getGameByUser(Player player) {
        List<Game> games = activeGames.stream().filter(game -> game.members.contains(player)).toList();
        if (games.isEmpty()) return null;
        return games.getFirst();
    }

    private static ItemStack setLeatherColor(ItemStack itemStack, Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
}
