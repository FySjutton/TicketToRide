package avox.test.ticketToRide.game;

import org.bukkit.entity.Player;

import java.util.*;

public class DestinationCard {
    public City pointA;
    public City pointB;
    public int reward;
    public boolean finished = false;

    public DestinationCard(City pointA, City pointB, int reward) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.reward = reward;
    }

    public static DestinationCard getDestinationCard(GameMap map) {
        Random rand = new Random();
        int indexA = rand.nextInt(map.cities.size());
        int indexB;

        do {
            indexB = rand.nextInt(map.cities.size());
        } while (indexB == indexA);

        int reward = new RewardCalculator().getReward(map, map.cities.get(indexA), map.cities.get(indexB));
        return new DestinationCard(map.cities.get(indexA), map.cities.get(indexB), reward);
    }

    public boolean isFinished() {
        return finished;
    }

    public int getReward() {
        return reward;
    }
}
