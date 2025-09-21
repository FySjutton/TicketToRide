package avox.test.ticketToRide.game.player;

import avox.test.ticketToRide.TicketToRide;
import avox.test.ticketToRide.game.DestinationCard;
import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.MapColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
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

    public LinkedHashMap<MapColor, Integer> cards = new LinkedHashMap<>();
    public int points = 0;
    public int trains = 45;

    public ItemDisplay beacon1;
    public ItemDisplay beacon2;
    public int beaconSlot;

    public ArrayList<DestinationCard> destinationCards = new ArrayList<>();

    public GamePlayer(Game game, Player player) {
        this.player = player;
        this.game = game;

        List<Pair<Color, Material>> usedColors = game.gamePlayers.values().stream().map(p -> p.markerData).toList();
        List<Pair<Color, Material>> validOptions = new ArrayList<>(markerColors.stream().filter(color -> !usedColors.contains(color)).toList());
        Collections.shuffle(validOptions);
        this.markerData = validOptions.getFirst();

        game.gameMap.colors.forEach(map -> cards.put(map, 0));
        cards.put(game.gameMap.wildCard, 0);
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

    public void setBeacons(int slot, int x1, int y1, int x2, int y2) {
        beaconSlot = slot;
        beacon1 = setBeacon(x1, y1, beacon1);
        beacon2 = setBeacon(x2, y2, beacon2);
    }

    public void clearBeacons() {
        if (beacon1 != null) {
            beacon1.remove();
        }
        if (beacon2 != null) {
            beacon2.remove();
        }
        beacon1 = null;
        beacon2 = null;
        beaconSlot = -1;
    }

    private ItemDisplay setBeacon(int x, int y, ItemDisplay beacon) {
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

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hideEntity(TicketToRide.plugin, beacon);
        }
        player.showEntity(TicketToRide.plugin, beacon);

        return beacon;
    }
}
