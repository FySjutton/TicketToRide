package avox.test.ticketToRide.util.board;

import avox.test.ticketToRide.game.core.arena.Arena;
import avox.test.ticketToRide.game.core.game.GameMap;
import avox.test.ticketToRide.game.core.Route;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TrainManager {
    public static void spawnSmallTrainCar(Arena arena, GameMap map, Route.Tile tile, Material material) {
        GameMap.TileMap tileMap = map.tileMaps.get(tile.rotation);
        Location spawnLocation = map.getStartLocation(arena).clone().add((double) (tile.x + tileMap.centerX) / 128, 0.05, (double) (tile.y + tileMap.centerY) / 128);

        ItemDisplay cart = (ItemDisplay) arena.world.spawnEntity(spawnLocation, EntityType.ITEM_DISPLAY);
        cart.setItemStack(new ItemStack(material));

        float pixel = 1f / 128f;

        Quaternionf rotation = new Quaternionf().rotateY((float) Math.toRadians(-tile.rotation));
        cart.setTransformation(new Transformation(
                new Vector3f(0f, 0f, 0f),
                rotation,
                new Vector3f(pixel * tile.width, 0.1f, pixel * tile.height),
                new Quaternionf()
        ));

        cart.setBillboard(Display.Billboard.FIXED);
        cart.setViewRange(20);
        cart.setPersistent(true);
    }
}
