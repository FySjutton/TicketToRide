package avox.test.ticketToRide.game;

import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameMap {
    public String name;
    public String version;
    public String description;

    public File map;

    public ArrayList<City> cities;
    public ArrayList<City> allCities = new ArrayList<>();

    public ArrayList<Color> colors = new ArrayList<>();
    public ArrayList<Route> routes = new ArrayList<>();
    public HashMap<Integer, TileMap> tileMaps = new HashMap<>();
    public HashMap<Integer, PointSquare> pointBoard = new HashMap<>();
    public ArrayList<LengthPoints> mapPoints;

    public int height;
    public int width;

    public int tilesX;
    public int tilesY;

    public int pointBoardSize;
    public String headTexture;

    public GameMap(String name, String description, File map, String version, int height, int width, int tilesX, int tilesY, int pointBoardSize, String headTexture) {
        this.name = name;
        this.description = description;
        this.map = map;
        this.version = version;
        this.height = height;
        this.width = width;
        this.tilesX = tilesX;
        this.tilesY = tilesY;
        this.pointBoardSize = pointBoardSize;
        this.headTexture = headTexture;
    }

    public City getCity(String name) {
        return allCities.stream().filter(city -> city.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Color getColor(String name) {
        return colors.stream().filter(color -> color.color.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Location getStartLocation(Arena arena) {
        return arena.mapStartPosition.clone().add((double) ((tilesX * 128 - width) / 2) / 128, 0, (double) ((tilesY * 128 -height) / 2) / 128);
    }

    public City getRandomCity(boolean includeSubCities) {
        if (includeSubCities) {
            return allCities.get(new Random().nextInt(0, allCities.size()));
        }
        return cities.get(new Random().nextInt(0, cities.size()));
    }

    public record Color(String color, Material material) {}

    public static class TileMap {
        public ArrayList<LineMap> lines = new ArrayList<>();
        public int centerX;
        public int centerY;

        public TileMap(int centerX, int centerY) {
            this.centerX = centerX;
            this.centerY = centerY;
        }

        public static class LineMap {
            public int start;
            public int length;

            public LineMap(int start, int length) {
                this.start = start;
                this.length = length;
            }
        }

        public boolean hit(int x, int y) {
            LineMap lineMap = lines.get(y);
            if (lineMap.start == -1) return false;
            return lineMap.start <= x && x <= lineMap.start + lineMap.length;
        }
    }

    public static class PointSquare {
        public int x;
        public int y;
        public int width;
        public int height;

        public PointSquare(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static class LengthPoints {
        public int min;
        public int max;
        public int points;

        public LengthPoints(int min, int max, int points) {
            this.min = min;
            this.max = max;
            this.points = points;
        }

        public boolean matches(int length) {
            return length >= min && length <= max;
        }
    }
}
