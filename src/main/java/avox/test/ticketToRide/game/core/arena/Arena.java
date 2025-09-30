package avox.test.ticketToRide.game.core.arena;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;

public class Arena {
    public String name;
    public String mapID;
    public World world;
    public Location spawnPosition;
    public Location mapStartPosition;

    public int tileX;
    public int tileY;

    public ArrayList<Location> billboards = new ArrayList<>();

    public Arena(String name, String mapID, World world, Location spawnPosition, Location mapStartPosition, int tileX, int tileY) {
        this.name = name;
        this.mapID = mapID;
        this.world = world;
        this.spawnPosition = spawnPosition;
        this.mapStartPosition = mapStartPosition;
        this.tileX = tileX;
        this.tileY = tileY;
    }
}
