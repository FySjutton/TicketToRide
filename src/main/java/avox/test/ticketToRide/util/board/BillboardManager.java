package avox.test.ticketToRide.util.board;

import avox.test.ticketToRide.game.core.arena.Arena;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.map.MinecraftFont;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class BillboardManager {
    public void summonBillboards(Arena arena) {
        spawnBillboardSection(arena.world, arena.billboards.get(0),
                "Goal", List.of(
                        "Each player starts with 45 trains.",
                        "You play on an interactive map.",
                        "Claim routes between cities to score points.",
                        "Complete destination tickets by connecting",
                        "the two cities listed on each ticket.",
                        "Longer routes give more points.",
                        "Finish tickets for bonuses, incomplete tickets",
                        "will cost you points at the end.",
                        "The player with the highest score wins."
                )
        );

        spawnBillboardSection(arena.world, arena.billboards.get(1),
                "Your Turn", List.of(
                        "On your turn, choose one action:",
                        "1) Take two train cards from the card inventory.",
                        "   You can pick cards of any color available.",
                        "2) Draw destination tickets and keep at least one.",
                        "3) Claim a route by spending matching train cards.",
                        "Use the map and inventories to make your moves.",
                        "Plan your routes carefully to complete tickets."
                )
        );

        spawnBillboardSection(arena.world, arena.billboards.get(2),
                "Routes & Tickets", List.of(
                        "Routes have lengths from 1 to 6 segments.",
                        "Longer routes are worth more points.",
                        "Destination tickets show two cities to connect.",
                        "Complete tickets earn you bonus points.",
                        "Unfinished tickets will subtract points.",
                        "Claim routes that help you finish your tickets.",
                        "Block opponents by taking key routes early."
                )
        );

        spawnBillboardSection(arena.world, arena.billboards.get(3),
                "End & Scoring", List.of(
                        "The game ends when a player",
                        "has 2 or fewer trains left.",
                        "Each player then gets one last turn.",
                        "Count points from claimed routes and tickets.",
                        "The longest continuous route gets a 10-point bonus.",
                        "The player with the highest total score wins."
                )
        );
    }

    private void spawnBillboardSection(World world, Location location, String heading, List<String> lines) {
        System.out.println(location);
        spawnLine(world, location,
                Component.text(heading)
                        .decorate(TextDecoration.BOLD)
                        .decorate(TextDecoration.UNDERLINED),
                2.0f, -1, true, false);

        String bodyText = String.join("\n", lines);
        spawnLine(world, location, Component.text(bodyText), 1.0f, lines.size(), true, false);
    }

    public TextDisplay spawnLine(World world, Location loc, Component text, float scale, int lines, boolean fixed, boolean background) {
        return world.spawn(loc, TextDisplay.class, display -> {
            if (fixed) {
                display.setBillboard(Display.Billboard.FIXED);
            } else {
                display.setBillboard(Display.Billboard.CENTER);
            }
            if (!background) {
                display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            }

            display.setSeeThrough(false);
            display.setPersistent(true);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setLineWidth(MinecraftFont.Font.getWidth(text.toString()));
            display.text(text);

            display.setTransformation(new Transformation(
                    new Vector3f(0, lines != -1 ? -(MinecraftFont.Font.getHeight() * lines / 16f) / 2 : 0, 0),
                    new Quaternionf(0, 0, 0, 1),
                    new Vector3f(scale, scale, scale),
                    new Quaternionf(0, 0, 0, 1)
            ));
        });
    }
}
