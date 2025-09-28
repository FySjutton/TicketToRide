package avox.test.ticketToRide.guis;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.ArrayList;

public class ActionManager {
    public HashMap<Integer, ArrayList<GuiAction>> actions = new HashMap<>();

    public void addAction(int slot, GuiAction action) {
        actions.computeIfAbsent(slot, k -> new ArrayList<>()).add(action);
    }

    public void addAction(Inventory inventory, ItemStack stack, int slot, GuiAction action) {
        addAction(slot, action);
        inventory.setItem(slot, stack);
    }

    public void removeAction(int slot) {
        actions.remove(slot);
    }

    public void clear() {
        actions.clear();
    }
}
