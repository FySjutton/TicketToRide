package avox.test.ticketToRide.game;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Map;

public class Route {
    public City point_a;
    public City point_b;

    public MapColor color;
    public ArrayList<Tile> tiles = new ArrayList<>();
    public int length;

    public Route(City point_a, City point_b, MapColor color) {
        this.point_a = point_a;
        this.point_b = point_b;
        this.color = color;
    }

    public static class Tile {
        public int rotation;
        public int widthBounding;
        public int heightBounding;

        public int width;
        public int height;

        public int x;
        public int y;

        public TileType type;

        public Tile(int rotation, int widthBounding, int heightBounding, int width, int height, int x, int y, TileType type) {
            this.rotation = rotation;

            this.widthBounding = widthBounding;
            this.heightBounding = heightBounding;

            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }

    public enum TileType {
        NORMAL,
        BOAT
    }
}
