package avox.test.ticketToRide.util.board;

import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.core.game.GameMap;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MarkerManager {
    public ItemDisplay spawnMarker(Game game, Material material) {
        ItemDisplay itemDisplay = (ItemDisplay) game.arena.world.spawnEntity(game.arena.mapStartPosition, EntityType.ITEM_DISPLAY);
        itemDisplay.setItemStack(new ItemStack(material));
        itemDisplay.setTransformation(new Transformation(
                new Vector3f(0f, 0f, 0f),
                new Quaternionf(),
                new Vector3f(0.2f, 0.06f, 0.2f),
                new Quaternionf()
        ));
        itemDisplay.setBillboard(Display.Billboard.FIXED);
        itemDisplay.setViewRange(64);
        itemDisplay.setPersistent(true);
        itemDisplay.setBrightness(new Display.Brightness(15, 15));
        return itemDisplay;
    }

    public void reposition(Game game, GamePlayer player, int points) {
        ItemDisplay marker = player.marker;
        int square = getSquare(points % game.gameMap.pointBoardSize);
        int stacked = Math.toIntExact(game.gamePlayers.values().stream().filter(p -> p.points != -1 && p.player != player.player && getSquare(p.points % game.gameMap.pointBoardSize) == square).count());

        GameMap.PointSquare pointSquare = game.gameMap.pointBoard.get(square);
        int x = pointSquare.x + pointSquare.width / 2;
        int y = pointSquare.y + pointSquare.height / 2;

        marker.teleport(game.gameMap.getStartLocation(game.arena).add((double) x / 128, 0.0525f + marker.getTransformation().getScale().y * stacked, (double) y / 128));
    }

    private int getSquare(int square) {
        return square == 0 ? 100 : square;
    }
}
