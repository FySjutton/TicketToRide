package avox.test.ticketToRide.game;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameMap {
    public String name;
    public String version;

    public ArrayList<City> cities = new ArrayList<>();
    public ArrayList<Color> colors = new ArrayList<>();
    public ArrayList<Route> routes = new ArrayList<>();
    public HashMap<Integer, TileMap> tileMaps = new HashMap<>();

    public int height;
    public int width;

    public int tilesX;
    public int tilesY;

    public GameMap(String name, String version, int height, int width, int tilesX, int tilesY) {
        this.name = name;
        this.version = version;
        this.height = height;
        this.width = width;
        this.tilesX = tilesX;
        this.tilesY = tilesY;
    }

    public City getCity(String name) {
        return cities.stream().filter(city -> city.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Color getColor(String name) {
        return colors.stream().filter(color -> color.color.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public record Color(String color, Material material) {}

    public static class TileMap {
        public ArrayList<LineMap> lines = new ArrayList<>();

        public static class LineMap {
            public int start;
            public int length;

            public LineMap(int start, int length) {
                this.start = start;
                this.length = length;
            }
        }
    }
}
