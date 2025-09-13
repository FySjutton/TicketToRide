package avox.test.ticketToRide.renderer;

import avox.test.ticketToRide.game.Arena;
import avox.test.ticketToRide.game.GameMap;
import avox.test.ticketToRide.game.Route;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TrainRenderer {
    public void spawnSmallTrainCar(Arena arena, GameMap map, Route.Tile tile) {
        GameMap.TileMap tileMap = map.tileMaps.get(tile.rotation);
        Location spawnLocation = map.getStartLocation(arena).clone().add((double) (tile.x + tileMap.centerX) / 128, 0.05, (double) (tile.y + tileMap.centerY) / 128);

        ItemDisplay cart = (ItemDisplay) arena.world.spawnEntity(spawnLocation, EntityType.ITEM_DISPLAY);
        cart.setItemStack(new ItemStack(Material.RED_CONCRETE));

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
