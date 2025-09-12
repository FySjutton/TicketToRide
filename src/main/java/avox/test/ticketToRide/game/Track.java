package avox.test.ticketToRide.game;

import avox.test.ticketToRide.game.player.GamePlayer;
import org.joml.Vector2d;

import java.util.ArrayList;

public class Track {
    public ArrayList<Subtrack> subtracks = new ArrayList<>();
    public City pointA;
    public City pointB;

    public Track(City pointA, City pointB) {
        this.pointA = pointA;
        this.pointB = pointB;
    }

    public static class Subtrack {
        public ArrayList<Part> parts;
        public int type;
        public GamePlayer ownedBy;

        public Subtrack(ArrayList<Part> parts, int type) {
            this.parts = parts;
            this.type = type;
        }
    }

    public static class Part {
        public Vector2d pos;
        public Rotation rotation;

        public Part(Vector2d pos, Rotation rotation) {
            this.pos = pos;
            this.rotation = rotation;
        }
    }

    public enum Rotation {
        HORIZONTAL,
        VERTICAL,
        DIAGONAL_POS,
        DIAGONAL_NEG
    }
}
