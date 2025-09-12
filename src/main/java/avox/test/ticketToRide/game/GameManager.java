package avox.test.ticketToRide.game;

import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.renderer.MapSummoner;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static avox.test.ticketToRide.TicketToRide.playerStateManager;


public class GameManager {
    public static ArrayList<Player> activePlayers = new ArrayList<>();
    public static ArrayList<Game> activeGames = new ArrayList<>();

    public static boolean createGame(Player gameOwner, String map, String arena) {
        MapManager.loadMap()









        String instanceId = "game_" + UUID.randomUUID();
        World gameWorld;
        gameWorld = MapManager.loadMap("testMap", instanceId);

        Location base = new Location(gameWorld, -4, 98, -12);
        new MapSummoner().generateAndDisplay(gameWorld, base);
//        new TrainRenderer().spawnSmallTrainCar(gameWorld, base);

        new BillboardManager().summonBillboards(gameWorld);

        activeGames.add(new Game(gameOwner, gameWorld, base, instanceId));
    }

    public static void startGame(Game game) {
        game.started = true;

        MarkerManager markerManager = new MarkerManager();
        for (GamePlayer player : game.players) {
            player.player.getInventory().setBoots(setLeatherColor(new ItemStack(Material.LEATHER_BOOTS), player.markerData.getLeft()));
            player.player.getInventory().setLeggings(setLeatherColor(new ItemStack(Material.LEATHER_LEGGINGS), player.markerData.getLeft()));
            player.player.getInventory().setChestplate(setLeatherColor(new ItemStack(Material.LEATHER_CHESTPLATE), player.markerData.getLeft()));
            player.player.getInventory().setHelmet(setLeatherColor(new ItemStack(Material.LEATHER_HELMET), player.markerData.getLeft()));

            player.marker = markerManager.spawnMarker(game.world, game.topLeft, player.markerData.getRight());
            player.points = 0;
            markerManager.reposition(game, player, player.points);
        }
    }

    public static void deleteGame(Game game) {
        activeGames.remove(game);
        for (Player player : game.world.getPlayers()) {
            activePlayers.remove(player);
            playerStateManager.restorePlayer(player);
        }
        MapManager.unloadAndDeleteMap(game.mapInstance);
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
