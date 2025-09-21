package avox.test.ticketToRide.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MapColor {
    public final String name;
    public final Material material;
    public final Component colored;

    public MapColor(String name, Material material, Component colored) {
        this.name = name;
        this.material = material;
        this.colored = colored;
    }

    public static Component coloredTextFromString(String text, boolean rainbow) {
        String formatted = toTitleCase(text.replaceAll("_", " "));

        if (rainbow) {
            return MiniMessage.miniMessage().deserialize("<gradient:#F4D654:#319D26>" + text);
        } else {
            try {
                Color bukkitColor = DyeColor.valueOf(text.toUpperCase()).getColor();
                return Component.text(formatted).color(TextColor.color(bukkitColor.asRGB()));
            } catch (IllegalArgumentException e) {
                return Component.text(formatted).color(TextColor.color(0xFFFFFF));
            }
        }
    }

    private static String toTitleCase(String text) {
        return Arrays.stream(text.split("_"))
            .map(s -> s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }
}
