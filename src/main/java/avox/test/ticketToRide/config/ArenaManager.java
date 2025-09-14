package avox.test.ticketToRide.config;

import avox.test.ticketToRide.TicketToRide;
import avox.test.ticketToRide.game.BaseArena;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;

public class ArenaManager {
    public static ArrayList<BaseArena> arenas = new ArrayList<>();

    public static void loadArenas(File arenaFolder) {
        arenas.clear();
        File[] folders = arenaFolder.listFiles(File::isDirectory);
        if (folders != null) {
            for (File folder : folders) {
                arenas.add(convertFileToArena(folder));
            }
        }
    }

    public static World loadArena(String mapName, String instanceId) {
        File source = new File(TicketToRide.plugin.getDataFolder(), "arenas/" + mapName + "/world");
        File target = new File(Bukkit.getWorldContainer(), instanceId);

        copyArenaWorld(source.toPath(), target.toPath());

        WorldCreator creator = new WorldCreator(instanceId);
        World world = Bukkit.createWorld(creator);

        world.setAutoSave(false);
        return world;
    }

    public static void unloadAndDeleteArenaWorld(String instanceId) {
        World world = Bukkit.getWorld(instanceId);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }

        File dir = new File(Bukkit.getWorldContainer(), instanceId);
        deleteDirectory(dir.toPath());
    }

    private static void copyArenaWorld(Path source, Path target) {
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

    public static void cleanOldArenaWorlds() {
        File worldContainer = Bukkit.getWorldContainer();
        File[] files = worldContainer.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory() && file.getName().startsWith("t2r_game_")) {
                deleteDirectory(file.toPath());
            }
        }
    }

    public static BaseArena convertFileToArena(File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Expected folder, got file: " + folder.getPath());
        }

        File dataFile = new File(folder, "data.json");

        if (!dataFile.exists()) {
            throw new IllegalStateException("Folder is missing required files!");
        }


        try (FileReader dataReader = new FileReader(dataFile)) {
            JsonObject dataJson = JsonParser.parseReader(dataReader).getAsJsonObject();
            return loadArena(folder.getName(), dataJson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static BaseArena loadArena(String fileName, JsonObject data) {
        JsonArray spawn = data.get("spawn").getAsJsonArray();
        JsonArray mapOrigin = data.get("map_origin").getAsJsonArray();

        BaseArena arena = new BaseArena(
            data.get("name").getAsString(),
            fileName,
            data.get("texture").getAsString(),
            data.get("description").getAsString(),
            new Location(null, spawn.get(0).getAsFloat(), spawn.get(1).getAsFloat(), spawn.get(2).getAsFloat(), spawn.get(3).getAsFloat(), spawn.get(4).getAsFloat()),
            new Location(null, mapOrigin.get(0).getAsFloat(), mapOrigin.get(1).getAsFloat(), mapOrigin.get(2).getAsFloat()),
            data.get("size_x").getAsInt(),
            data.get("size_y").getAsInt()
        );

        JsonArray billboardJson = data.get("billboards").getAsJsonArray();
        for (JsonElement billboard : billboardJson.asList()) {
            JsonArray billboardArray = billboard.getAsJsonArray();
            arena.billboards.add(new Location(null, billboardArray.get(0).getAsFloat(), billboardArray.get(1).getAsFloat(), billboardArray.get(2).getAsFloat(), billboardArray.get(3).getAsFloat(), 0));
        }
        return arena;
    }
}
