package avox.test.ticketToRide.game;

import avox.test.ticketToRide.game.core.City;
import avox.test.ticketToRide.game.core.Route;
import avox.test.ticketToRide.game.core.game.GameMap;
import avox.test.ticketToRide.game.core.game.GamePlayer;

import java.util.*;

public class RewardCalculator {
    private static Map<City, List<Route>> buildGraph(GameMap gameMap) {
        List<Route> routes = gameMap.routes.stream().map(route -> new Route(route.point_a, route.point_b, route.length)).toList();
        Map<City, List<Route>> graph = new HashMap<>();
        for (City city : gameMap.allCities) graph.put(city, new ArrayList<>());
        for (Route route : routes) {
            graph.get(route.point_a).add(route);
            graph.get(route.point_b).add(new Route(route.point_b, route.point_a, route.length));
        }
        return graph;
    }

    public static int getReward(GameMap gameMap, City start, City goal) {
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

    public static int getLongestContinuousRoute(GamePlayer player) {
        List<Route> routes = player.routes.stream().map(route -> new Route(route.point_a, route.point_b, route.length)).toList();
        Map<City, List<Route>> graph = new HashMap<>();
        for (Route r : routes) {
            graph.computeIfAbsent(r.point_a, k -> new ArrayList<>()).add(r);
            graph.computeIfAbsent(r.point_b, k -> new ArrayList<>()).add(r);
        }

        int longest = 0;

        for (City start : graph.keySet()) {
            longest = Math.max(longest, depthFirstSearch(start, new HashSet<>(), graph));
        }

        return longest;
    }

    private static int depthFirstSearch(City current, Set<Route> usedRoutes, Map<City, List<Route>> graph) {
        int max = 0;

        for (Route route : graph.getOrDefault(current, List.of())) {
            if (usedRoutes.contains(route)) continue;

            usedRoutes.add(route);

            City next = (route.point_a.equals(current)) ? route.point_b : route.point_a;

            int length = route.length + depthFirstSearch(next, usedRoutes, graph);
            max = Math.max(max, length);

            usedRoutes.remove(route);
        }

        return max;
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

    private static int scoreForLength(int length, ArrayList<GameMap.LengthPoints> mapPoints) {
        for (GameMap.LengthPoints lengthPoint : mapPoints) {
            if (lengthPoint.matches(length)) {
                return lengthPoint.points;
            }
        }
        return 0;
    }
}
