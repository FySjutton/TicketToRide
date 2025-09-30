package avox.test.ticketToRide.game.core.arena;

import avox.test.ticketToRide.config.ArenaManager;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.UUID;

public class BaseArena {
    public String name;
    public String fileName;
    public String description;
    public String texture;
    public Location spawnPosition;
    public Location mapStartPosition;

    public int tileX;
    public int tileY;

    public ArrayList<Location> billboards = new ArrayList<>();

    public BaseArena(String name, String fileName, String texture, String description, Location spawnPosition, Location mapStartPosition, int tileX, int tileY) {
        this.name = name;
        this.fileName = fileName;
        this.texture = texture;
        this.description = description;
        this.spawnPosition = spawnPosition;
        this.mapStartPosition = mapStartPosition;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public Arena initiateArena() {
        String instanceId = "t2r_game_" + UUID.randomUUID();

        World world = ArenaManager.loadArena(fileName, instanceId);

        spawnPosition = spawnPosition.toLocation(world);
        mapStartPosition = mapStartPosition.toLocation(world);

        Arena arena = new Arena(name, instanceId, world, spawnPosition, mapStartPosition, tileX, tileY);

        for (Location billboard : billboards) {
            arena.billboards.add(billboard.toLocation(world));
        }
        return arena;
    }
}
