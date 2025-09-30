package avox.test.ticketToRide.game;

import avox.test.ticketToRide.game.core.City;
import avox.test.ticketToRide.game.core.game.GameMap;

import java.util.*;

public class RewardCalculator {
    private Map<City, List<Route>> buildGraph(GameMap gameMap) {
        List<Route> routes = gameMap.routes.stream().map(route -> new Route(route.point_a, route.point_b, route.length)).toList();
        Map<City, List<Route>> graph = new HashMap<>();
        for (City city : gameMap.allCities) graph.put(city, new ArrayList<>());
        for (Route route : routes) {
            graph.get(route.point_a).add(route);
            graph.get(route.point_b).add(new Route(route.point_b, route.point_a, route.length));
        }
        return graph;
    }

    public int getReward(GameMap gameMap, City start, City goal) {
        if (start == goal) return 0;

        Map<City, Integer> dist = new HashMap<>();
        for (City c : gameMap.allCities) dist.put(c, Integer.MAX_VALUE);
        dist.put(start, 0);

        PriorityQueue<City> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        pq.add(start);

        Map<City, List<Route>> graph = buildGraph(gameMap);

        while (!pq.isEmpty()) {
            City current = pq.poll();
            int d = dist.get(current);

            if (current == goal) break;

            for (Route r : graph.get(current)) {
                City neighbor = r.point_b;
                int newDist = d + r.length;
                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    pq.add(neighbor);
                }
            }
        }

        return scoreForLength(dist.get(goal), gameMap.mapPoints);
    }

    static class Route {
        public City point_a;
        public City point_b;
        public int length;

        public Route(City a, City b, int length) {
            this.point_a = a;
            this.point_b = b;
            this.length = length;
        }
    }

    private int scoreForLength(int length, ArrayList<GameMap.LengthPoints> mapPoints) {
        for (GameMap.LengthPoints lengthPoint : mapPoints) {
            if (lengthPoint.matches(length)) {
                return lengthPoint.points;
            }
        }
        return 0;
    }
}
