package avox.test.ticketToRide.guis.createGame;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.j2objc.annotations.Property;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class GuiTools {
    public static ItemStack createHead(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        ResolvableProfile profile = ResolvableProfile.resolvableProfile()
            .addProperty(new ProfileProperty("textures", base64))
            .build();

        head.setData(DataComponentTypes.PROFILE, profile);
        return head;
    }
}
