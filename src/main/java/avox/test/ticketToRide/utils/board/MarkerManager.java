package avox.test.ticketToRide.utils.board;

import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.player.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MarkerManager {
    public BlockDisplay spawnMarker(World world, Location loc, Material material) {
        BlockDisplay blockDisplay = (BlockDisplay) world.spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        blockDisplay.setBlock(material.createBlockData());
        blockDisplay.setTransformation(new Transformation(
                new Vector3f(0f, 0f, 0f),
                new Quaternionf(),
                new Vector3f(0.2f, 0.06f, 0.2f),
                new Quaternionf()
        ));
        blockDisplay.setBillboard(Display.Billboard.FIXED);
        blockDisplay.setViewRange(64);
        blockDisplay.setPersistent(true);
        blockDisplay.setBrightness(new Display.Brightness(15, 15));
        return blockDisplay;
    }

    public void reposition(Game game, GamePlayer player, int points) {
        BlockDisplay marker = player.marker;
        int square = points % 100;
        int stacked = Math.toIntExact(game.players.stream().filter(p -> p.points != -1 && p.player != player.player && p.points % 100 == square).count());

        int x;
        int y;
        if (square <= 25) {
            x = game.tilesX * 128 - 18 - 19;
            y = 18 + square * 38 + 19;
        } else if (square <= 50) {
            x = game.tilesX * 128 - 18 - (square - 25) * 38 - 19;
            y = game.tilesX * 128 - 18 - 19;
        } else if (square <= 75) {
            x = 18 + 19;
            y = game.tilesX * 128 - 18 - (square - 49) * 38 + 19;
        } else {
            x = 18 + (square - 75) * 38 + 19;
            y = 18 + 19;
        }

        double offset = marker.getTransformation().getScale().x / 2;
        marker.teleport(game.topLeft.clone().add((double) x / 128 - offset, marker.getTransformation().getScale().y * stacked, (double) y / 128 - offset));
    }
}
