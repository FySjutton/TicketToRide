package avox.test.ticketToRide.game;

import avox.test.ticketToRide.config.ArenaManager;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.UUID;

public class ArenaBase {
    public String name;
    public Location spawnPosition;
    public Location mapStartPosition;

    public int tileX;
    public int tileY;

    public ArrayList<Location> billboards = new ArrayList<>();

    public ArenaBase(String name, Location spawnPosition, Location mapStartPosition, int tileX, int tileY) {
        this.name = name;
        this.spawnPosition = spawnPosition;
        this.mapStartPosition = mapStartPosition;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public Arena initiateArena() {
        String instanceId = "t2r_game_" + UUID.randomUUID();

        World world = ArenaManager.loadArena(name, instanceId);
        Arena arena = new Arena(name, instanceId, world, spawnPosition, mapStartPosition, tileX, tileY);
        arena.billboards = billboards;
        return arena;
    }
}
