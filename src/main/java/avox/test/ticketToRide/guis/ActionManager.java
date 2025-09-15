package avox.test.ticketToRide.guis;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ActionManager {
    public HashMap<Integer, GuiAction> actions = new HashMap<>();

    public void setSlot(int slot, GuiAction action) {
        actions.put(slot, action);
    }

    public void setSlot(Inventory inventory, ItemStack stack, int slot, GuiAction action) {
        actions.put(slot, action);
        inventory.setItem(slot, stack);
    }

    public void clear() {
        actions.clear();
    }
}
