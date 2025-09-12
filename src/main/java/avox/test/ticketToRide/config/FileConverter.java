package avox.test.ticketToRide.config;

import avox.test.ticketToRide.game.City;
import avox.test.ticketToRide.game.GameMap;
import avox.test.ticketToRide.game.Route;
import com.google.gson.*;
import org.bukkit.Material;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

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

            return generateMap(dataJson, tilesJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static GameMap generateMap(JsonObject data, JsonObject tileMapData) {
        GameMap map = new GameMap(data.get("map").getAsString(), data.get("version").getAsString(), data.get("size_x").getAsInt(), data.get("size_y").getAsInt(), data.get("map_x").getAsInt(), data.get("map_y").getAsInt());

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
                route.tiles.add(new Route.Tile(tileData.get("rotation").getAsInt(), tileData.get("width").getAsInt(), tileData.get("height").getAsInt(), tileData.get("x").getAsInt(), tileData.get("y").getAsInt(), Route.TileType.valueOf(tileData.get("type").getAsString().toUpperCase())));
            }

            map.routes.add(route);
        }

        for (String tile : tileMapData.keySet()) {
            GameMap.TileMap tileMap = new GameMap.TileMap();
            JsonArray tileData = tileMapData.getAsJsonArray(tile);
            for (JsonElement tileRotation : tileData) {
                JsonArray rotation = tileRotation.getAsJsonArray();
                tileMap.lines.add(new GameMap.TileMap.LineMap(rotation.get(0).getAsInt(), rotation.get(1).getAsInt()));
            }
        }

        return map;
    }
}
