package avox.test.ticketToRide.utils;

import avox.test.ticketToRide.TicketToRide;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

public class MapManager {
    public static World loadMap(String mapName, String instanceId) {
        File source = new File(TicketToRide.plugin.getDataFolder(), "maps/templates/" + mapName + "/world");
        File target = new File(Bukkit.getWorldContainer(), instanceId);

        copyWorld(source.toPath(), target.toPath());

        WorldCreator creator = new WorldCreator(instanceId);
        World world = Bukkit.createWorld(creator);

        world.setAutoSave(false);
        return world;
    }

    public static void unloadAndDeleteMap(String instanceId) {
        World world = Bukkit.getWorld(instanceId);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }

        File dir = new File(Bukkit.getWorldContainer(), instanceId);
        deleteDirectory(dir.toPath());
    }

    private static void copyWorld(Path source, Path target) {
        try {
            Files.walk(source).forEach(path -> {
                try {
                    Path relative = source.relativize(path);
                    Path dest = target.resolve(relative);
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteDirectory(Path path) {
        if (!Files.exists(path)) return;
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cleanOldMaps() {
        File worldContainer = Bukkit.getWorldContainer();
        File[] files = worldContainer.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory() && file.getName().startsWith("game_")) {
                deleteDirectory(file.toPath());
            }
        }
    }
}
