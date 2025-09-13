package avox.test.ticketToRide.config;

import avox.test.ticketToRide.game.Arena;
import avox.test.ticketToRide.game.City;
import avox.test.ticketToRide.game.GameMap;
import avox.test.ticketToRide.game.Route;
import com.google.gson.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class FileConverter {
    private static final Gson gson = new Gson();

    public static GameMap convertFileToMap(File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Expected folder, got file: " + folder.getPath());
        }

        File dataFile = new File(folder, "data.json");
        File mapImage = new File(folder, "map.png");
        File tilesFile = new File(folder, "mapped_tiles.json");

        if (!dataFile.exists() || !mapImage.exists() || !tilesFile.exists()) {
            throw new IllegalStateException("Folder is missing required files!");
        }

        try (FileReader dataReader = new FileReader(dataFile);
             FileReader tilesReader = new FileReader(tilesFile)) {

            JsonObject dataJson = JsonParser.parseReader(dataReader).getAsJsonObject();
            JsonObject tilesJson = JsonParser.parseReader(tilesReader).getAsJsonObject();

            return generateMap(mapImage, dataJson, tilesJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static GameMap generateMap(File mapImage, JsonObject data, JsonObject tileMapData) {
        GameMap map = new GameMap(data.get("map").getAsString(), mapImage, data.get("version").getAsString(), data.get("size_x").getAsInt(), data.get("size_y").getAsInt(), data.get("map_x").getAsInt(), data.get("map_y").getAsInt(), data.get("point_board_size").getAsInt());

        JsonObject colors = data.getAsJsonObject("colors");
        for (Map.Entry<String, JsonElement> color : colors.asMap().entrySet()) {
            map.colors.add(new GameMap.Color(color.getKey(), Material.valueOf(color.getValue().getAsString().toUpperCase())));
        }

        JsonObject cities = data.getAsJsonObject("cities");
        for (String city : cities.keySet()) {
            JsonObject cityJson = cities.getAsJsonObject(city);
            map.cities.add(new City(city, cityJson.get("height").getAsInt(), cityJson.get("width").getAsInt(), cityJson.get("x").getAsInt(), cityJson.get("y").getAsInt(), cityJson.get("type").getAsString().equals("city")));
        }

        JsonArray routes = data.getAsJsonArray("routes");
        for (JsonElement jsonRoute : routes) {
            JsonObject routeData = jsonRoute.getAsJsonObject();
            Route route = new Route(map.getCity(routeData.get("point_a").getAsString()), map.getCity(routeData.get("point_b").getAsString()), map.getColor(routeData.get("color").getAsString()));

            JsonArray tiles = routeData.getAsJsonArray("tiles");
            for (JsonElement jsonTile : tiles) {
                JsonObject tileData = jsonTile.getAsJsonObject();
                route.tiles.add(new Route.Tile(tileData.get("rotation").getAsInt(), tileData.get("width").getAsInt(), tileData.get("height").getAsInt(), data.get("tile_x").getAsInt(), data.get("tile_y").getAsInt(), tileData.get("x").getAsInt(), tileData.get("y").getAsInt(), Route.TileType.valueOf(tileData.get("type").getAsString().toUpperCase())));
            }

            map.routes.add(route);
        }

        JsonObject pointBoard = data.getAsJsonObject("point_board");
        for (String key : pointBoard.keySet()) {
            JsonObject pointBoardJson = pointBoard.getAsJsonObject(key);
            map.pointBoard.put(Integer.parseInt(key), new GameMap.PointSquare(pointBoardJson.get("x").getAsInt(), pointBoardJson.get("y").getAsInt(), pointBoardJson.get("width").getAsInt(), pointBoardJson.get("height").getAsInt()));
        }

        for (String tile : tileMapData.keySet()) {
            JsonArray tileTopLeft = tileMapData.getAsJsonObject(tile).get("center").getAsJsonArray();
            JsonArray tileData = tileMapData.getAsJsonObject(tile).get("rows").getAsJsonArray();
            GameMap.TileMap tileMap = new GameMap.TileMap(tileTopLeft.get(0).getAsInt(), tileTopLeft.get(1).getAsInt());
            for (JsonElement tileRotation : tileData) {
                if (tileRotation.isJsonNull()) {
                    tileMap.lines.add(new GameMap.TileMap.LineMap(-1, -1));
                } else {
                    JsonArray rotation = tileRotation.getAsJsonArray();
                    tileMap.lines.add(new GameMap.TileMap.LineMap(rotation.get(0).isJsonNull() ? -1 : rotation.get(0).getAsInt(), rotation.get(1).getAsInt()));
                }
            }
            map.tileMaps.put(Integer.parseInt(tile.split("_")[1]), tileMap);
        }

        return map;
    }

    public static Arena convertFileToArena(String arenaName, File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Expected folder, got file: " + folder.getPath());
        }

        File dataFile = new File(folder, "data.json");

        if (!dataFile.exists()) {
            throw new IllegalStateException("Folder is missing required files!");
        }

        String instanceId = "game_" + UUID.randomUUID();
        World world = ArenaManager.loadArena(arenaName, instanceId);

        try (FileReader dataReader = new FileReader(dataFile)) {
            JsonObject dataJson = JsonParser.parseReader(dataReader).getAsJsonObject();
             return generateArena(dataJson, world, instanceId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Arena generateArena(JsonObject data, World world, String mapID) {
        JsonArray spawn = data.get("spawn").getAsJsonArray();
        JsonArray mapOrigin = data.get("map_origin").getAsJsonArray();
        Arena arena = new Arena(
            data.get("name").getAsString(),
            mapID,
            world,
            new Location(world, spawn.get(0).getAsFloat(), spawn.get(1).getAsFloat(), spawn.get(2).getAsFloat(), spawn.get(3).getAsFloat(), spawn.get(4).getAsFloat()),
            new Location(world, mapOrigin.get(0).getAsFloat(), mapOrigin.get(1).getAsFloat(), mapOrigin.get(2).getAsFloat()),
            data.get("size_x").getAsInt(),
            data.get("size_y").getAsInt()
        );

        JsonArray billboards = data.get("billboards").getAsJsonArray();
        for (JsonElement billboardData : billboards) {
            JsonArray billboard = billboardData.getAsJsonArray();
            arena.billboards.add(new Location(world, billboard.get(0).getAsFloat(), billboard.get(1).getAsFloat(), billboard.get(2).getAsFloat(), billboard.get(3).getAsFloat(), 0));
        }

        return arena;
    }
}
