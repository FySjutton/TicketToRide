package avox.test.ticketToRide.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.Comparator;

public class LeaderboardMessage {
    public record Entry(String name, int points) {}

    public static Component buildLeaderboard(String title, ArrayList<Entry> entries) {
        entries.sort(Comparator.comparingInt((Entry e) -> e.points).reversed());

        TextComponent.Builder builder = Component.text()
                .append(Component.text(title + "\n", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true));

        int rank = 1;
        for (Entry e : entries) {
            builder.append(Component.text(rank + ". " + e.name + " - " + e.points + " points\n", NamedTextColor.GRAY));
            rank++;
        }

        return builder.build();
    }
}