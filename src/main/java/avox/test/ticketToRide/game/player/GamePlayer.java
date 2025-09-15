package avox.test.ticketToRide.game.player;

import avox.test.ticketToRide.game.DestinationCard;
import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.utils.board.MarkerManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class GamePlayer {
    public Player player;
    public Game game;
    public ItemDisplay marker;
    public Pair<Color, Material> markerData;


    public int points = -1; // Changes to 0 when game is started
    public int trains = 45;

    public ItemDisplay beacon1;
    public ItemDisplay beacon2;

    public ArrayList<DestinationCard> destinationCards = new ArrayList<>();

    public GamePlayer(Game game, Player player) {
        this.player = player;
        this.game = game;

        List<Pair<Color, Material>> usedColors = game.gamePlayers.values().stream().map(p -> p.markerData).toList();
        List<Pair<Color, Material>> validOptions = new ArrayList<>(markerColors.stream().filter(color -> !usedColors.contains(color)).toList());
        Collections.shuffle(validOptions);
        this.markerData = validOptions.getFirst();
    }

    private static final ArrayList<Pair<Color, Material>> markerColors = new ArrayList<>(
            List.of(
                    Pair.of(Color.BLUE, Material.BLUE_CONCRETE),
                    Pair.of(Color.GREEN, Material.GREEN_CONCRETE),
                    Pair.of(Color.RED, Material.RED_CONCRETE),
                    Pair.of(Color.PURPLE, Material.PURPLE_CONCRETE),
                    Pair.of(Color.YELLOW, Material.YELLOW_CONCRETE)
            )
    );

    public void setBeacon1(int x, int y) {
        setBeacon(x, y, beacon1);
    }

    public void setBeacon2(int x, int y) {
        setBeacon(x, y, beacon2);
    }

    private void setBeacon(int x, int y, ItemDisplay beacon) {
        if (beacon != null) {
            beacon.remove();
        }

        Location location = game.gameMap.getStartLocation(game.arena).clone().add((double) x / 128, 0.05, (double) y / 128);

        beacon = (ItemDisplay) game.arena.world.spawnEntity(location, EntityType.ITEM_DISPLAY);
        beacon.setItemStack(new ItemStack(Material.RED_CONCRETE));

        beacon.setTransformation(new Transformation(
                new Vector3f(0f, 1f, 0f),
                new Quaternionf(),
                new Vector3f(0.02f, 2f, 0.02f),
                new Quaternionf()
        ));

        beacon.setBillboard(Display.Billboard.FIXED);
        beacon.setViewRange(20);
        beacon.setPersistent(true);
    }
}
