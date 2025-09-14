package avox.test.ticketToRide.game;

import java.util.*;

public class DestinationCard {
    public City pointA;
    public City pointB;
    public int reward;

    public DestinationCard(City pointA, City pointB, int reward) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.reward = reward;
    }

    public static class PathFinder {
        private Map<City, List<Route>> buildGraph(GameMap gameMap) {
            List<Route> routes = gameMap.routes.stream().map(route -> new Route(route.point_a, route.point_b, route.length)).toList();
            Map<City, List<Route>> graph = new HashMap<>();
            for (City city : gameMap.cities) graph.put(city, new ArrayList<>());
            for (Route route : routes) {
                graph.get(route.point_a).add(route);
                graph.get(route.point_b).add(new Route(route.point_b, route.point_a, route.length));
            }
            return graph;
        }

        public int getReward(GameMap gameMap, City start, City goal) {
            if (start == goal) return 0;

            Map<City, Integer> dist = new HashMap<>();
            for (City c : gameMap.cities) dist.put(c, Integer.MAX_VALUE);
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

            int shortest = dist.get(goal);
            int reward = (shortest == 0) ? 0 : 1000 / shortest;
            return reward;
        }

        class Route {
            public City point_a;
            public City point_b;
            public int length;

            public Route(City a, City b, int length) {
                this.point_a = a;
                this.point_b = b;
                this.length = length;
            }
        }
    }
}
