package avox.test.ticketToRide.game;

public class DestinationCard {
    public City pointA;
    public City pointB;
    public int reward;
    public String imagePath;

    public DestinationCard(City pointA, City pointB, int reward, String imagePath) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.reward = reward;
        this.imagePath = imagePath;
    }
}
