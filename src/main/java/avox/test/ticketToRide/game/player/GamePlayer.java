package avox.test.ticketToRide.game.player;

import avox.test.ticketToRide.game.DestinationCard;
import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.utils.board.MarkerManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;

import java.util.*;

public class GamePlayer {
    public Player player;
    public BlockDisplay marker;
    public Pair<Color, Material> markerData;


    public int points = -1; // Changes to 0 when game is started
    public int trains = 45;

    public ArrayList<DestinationCard> destinationCards = new ArrayList<>();

    public GamePlayer(Game game, Player player) {
        this.player = player;

        List<Pair<Color, Material>> usedColors = game.players.stream().map(p -> p.markerData).toList();
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
}
