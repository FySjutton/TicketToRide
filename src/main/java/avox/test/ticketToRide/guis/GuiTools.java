package avox.test.ticketToRide.guis;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class GuiTools {
    public static ItemStack createHead(String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        String base64;
        if (texture.startsWith("http://") || texture.startsWith("https://") || !texture.startsWith("eyJ")) {
            if (!texture.startsWith("http")) {
                texture = "http://textures.minecraft.net/texture/" + texture;
            }
            String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + texture + "\"}}}";
            base64 = Base64.getEncoder().encodeToString(json.getBytes());
        } else {
            base64 = texture;
        }

        ResolvableProfile profile = ResolvableProfile.resolvableProfile()
            .addProperty(new ProfileProperty("textures", base64))
            .build();

        head.setData(DataComponentTypes.PROFILE, profile);
        return head;
    }

    public static ItemStack format(ItemStack stack, Component name, List<Component> lore) {
        stack.editMeta(meta -> meta.lore(lore));
        return format(stack, name);
    }

    public static ItemStack format(ItemStack stack, Component name) {
        stack.editMeta(meta -> meta.customName(name));
        return stack;
    }

    public static Component colorize(String text, TextColor color) {
        return colorize(Component.text(text), color);
    }

    public static Component colorize(Component component, TextColor color) {
        return component.color(color).decoration(TextDecoration.ITALIC, false);
    }

    public static Component getYellow(String text) {
        return colorize(text, NamedTextColor.YELLOW);
    }

    public static Component getGray(String text) {
        return colorize(text, NamedTextColor.GRAY);
    }
}
