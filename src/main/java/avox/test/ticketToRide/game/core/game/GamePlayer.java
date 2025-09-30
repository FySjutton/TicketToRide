package avox.test.ticketToRide.game.core.game;

import avox.test.ticketToRide.TicketToRide;
import avox.test.ticketToRide.game.core.DestinationCard;
import avox.test.ticketToRide.game.core.MapColor;
import org.bukkit.Bukkit;
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
    public MapColor markerData;

    public LinkedHashMap<MapColor, Integer> cards = new LinkedHashMap<>();
    public int points = -1;
    public int trains;

    public ItemDisplay beacon1;
    public ItemDisplay beacon2;
    public int beaconSlot;

    private final ArrayList<DestinationCard> destinationCards = new ArrayList<>();

    public GamePlayer(Game game, Player player) {
        this.player = player;
        this.game = game;

        List<MapColor> usedColors = game.gamePlayers.values().stream().map(p -> p.markerData).toList();
        List<MapColor> validOptions = new ArrayList<>(markerColors.stream().filter(color -> !usedColors.contains(color)).toList());
        Collections.shuffle(validOptions);
        this.markerData = validOptions.getFirst();

        game.gameMap.getAllColors().forEach(map -> cards.put(map, 0));
    }

    public ArrayList<DestinationCard> getDestinationCards() {
        return destinationCards;
    }

    public void addDestinationCard(DestinationCard card) {
        destinationCards.add(card);
        updateCardOrder();
    }

    public void updateCardOrder() {
        destinationCards.sort(Comparator
            .comparing(DestinationCard::isFinished)
            .thenComparing(DestinationCard::getReward));
    }

    private static final ArrayList<MapColor> markerColors = new ArrayList<>(
            List.of(
                    new MapColor("blue", Material.BLUE_CONCRETE),
                    new MapColor("green", Material.GREEN_CONCRETE),
                    new MapColor("red", Material.RED_CONCRETE),
                    new MapColor("purple", Material.PURPLE_CONCRETE),
                    new MapColor("yellow", Material.YELLOW_CONCRETE)
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
