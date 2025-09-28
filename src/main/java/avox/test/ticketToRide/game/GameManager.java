package avox.test.ticketToRide.game;

import avox.test.ticketToRide.config.ArenaManager;
import avox.test.ticketToRide.game.gameHandler.GameHandler;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.guis.PlayerGuiManager;
import avox.test.ticketToRide.renderer.MapSummoner;
import avox.test.ticketToRide.utils.BillboardManager;
import avox.test.ticketToRide.utils.board.MarkerManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static avox.test.ticketToRide.TicketToRide.playerStateManager;


public class GameManager {
    public static ArrayList<Player> activePlayers = new ArrayList<>();
    public static ArrayList<Game> activeGames = new ArrayList<>();

    public static void createGame(JavaPlugin plugin, Player gameOwner, GameMap gameMap, BaseArena arena) {
        try {
            Arena gameArena = arena.initiateArena();
            if (gameMap == null || gameArena == null) {
                return;
            }

            new MapSummoner().generateAndDisplay(gameArena.world, gameMap.map, gameArena.mapStartPosition, gameMap.tilesX, gameMap.tilesY);
            new BillboardManager().summonBillboards(gameArena);


            // Testing
//            TrainRenderer renderer = new TrainRenderer();
//            for (Route route : gameMap.routes) {
//                for (Route.Tile tile : route.tiles) {
//                    renderer.spawnSmallTrainCar(gameArena, gameMap, tile);
//                }
//            }

            activeGames.add(new Game(gameOwner, gameArena, gameMap));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void startGame(Game game) {
        game.started = true;
        game.gameHandler = new GameHandler(game);
        game.infoText.text(Component.empty());

        MarkerManager markerManager = new MarkerManager();
        for (GamePlayer player : game.gamePlayers.values()) {
            player.player.getInventory().setBoots(setLeatherColor(new ItemStack(Material.LEATHER_BOOTS), player.markerData.color));
            player.player.getInventory().setLeggings(setLeatherColor(new ItemStack(Material.LEATHER_LEGGINGS), player.markerData.color));
            player.player.getInventory().setChestplate(setLeatherColor(new ItemStack(Material.LEATHER_CHESTPLATE), player.markerData.color));
            player.player.getInventory().setHelmet(setLeatherColor(new ItemStack(Material.LEATHER_HELMET), player.markerData.color));

            player.marker = markerManager.spawnMarker(game, player.markerData.material);
            player.points = 0;
            player.trains = game.gameMap.startingTrains;
            markerManager.reposition(game, player, player.points);
        }
    }

    public static void deleteGame(Game game) {
        activeGames.remove(game);
        for (Player player : game.gamePlayers.keySet()) {
            activePlayers.remove(player);
            playerStateManager.restorePlayer(player);
            PlayerGuiManager.removeGui(player);
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
        List<Game> games = activeGames.stream().filter(game -> game.gamePlayers.containsKey(player)).toList();
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
