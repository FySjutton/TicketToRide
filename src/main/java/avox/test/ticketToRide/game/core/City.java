package avox.test.ticketToRide.game.core;

public record City(String name, int height, int width, int x, int y, boolean city) {}

// The city boolean is if it's a city, or a subcity (false).