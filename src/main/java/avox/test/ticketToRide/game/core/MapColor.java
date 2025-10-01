package avox.test.ticketToRide.game.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class MapColor {
    public final String name;
    public final Material material;
    public final Component colored;
    public final Color color;

    public MapColor(String name) {
        this(name, Material.WHITE_WOOL, Component.text(name), Color.WHITE);
    }

    public MapColor(String name, Material material) {
        this(name, material, coloredTextFromString(name, false), getColor(name));
    }

    public MapColor(String name, Material material, Component colored) {
        this(name, material, colored, getColor(name));
    }

    public MapColor(String name, Material material, Component colored, Color color) {
        this.name = name;
        this.material = material;
        this.colored = colored;
        this.color = color;
    }

    public static Component coloredTextFromString(String text, boolean rainbow) {
        String formatted = toTitleCase(text.replaceAll("_", " "));

        Component message;
        if (rainbow) {
            message = MiniMessage.miniMessage().deserialize("<gradient:#F4D654:#319D26>" + formatted);
        } else {
            message = Component.text(formatted).color(TextColor.color(getColor(text).asRGB()));
        }
        return message.decoration(TextDecoration.ITALIC, false);
    }

    private static Color getColor(String text) {
        try {
            return DyeColor.valueOf(text.toUpperCase()).getColor();
        } catch (IllegalArgumentException e) {
            return Color.WHITE;
        }
    }

    private static String toTitleCase(String text) {
        return Arrays.stream(text.split("_"))
            .map(s -> s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }
}
