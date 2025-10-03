package avox.test.ticketToRide.guis.general;

import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.GuiAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public abstract class CenteredRow<T> {
    private final ActionManager actionManager;
    private final Inventory inventory;
    private final int startSlot;
    private final int availableSlots;
    private final ArrayList<T> objects;

    public CenteredRow(Inventory inventory, int startSlot, int length, ArrayList<T> objectList) {
        this(null, inventory, startSlot, length, objectList);
    }

    public CenteredRow(ActionManager actionManager, Inventory inventory, int startSlot, int length, ArrayList<T> objectList) {
        this.actionManager = actionManager;
        this.inventory = inventory;
        this.startSlot = startSlot;
        this.availableSlots = length;
        this.objects = objectList;
        distributeItems();
    }

    public abstract ItemStack getSlotItem(T object);

    public abstract GuiAction getGuiAction(T object);

    private void setItem(T object, int slot) {
        if (slot < 0 || slot >= inventory.getSize()) return;

        if (actionManager != null) {
            GuiAction action = getGuiAction(object);
            if (action == null) {
                actionManager.removeAction(slot);
                inventory.setItem(slot, getSlotItem(object));
            } else {
                actionManager.setAction(inventory, getSlotItem(object), slot, action);
            }
        } else {
            inventory.setItem(slot, getSlotItem(object));
        }
    }

    private void distributeItems() {
        int objectCount = objects.size();
        if (objectCount == 0) return;

        if (objectCount >= availableSlots) {
            for (int i = 0; i < availableSlots; i++) {
                setItem(objects.get(i), startSlot + i);
            }
            return;
        }

        int rowStart = startSlot;
        int rowEnd = startSlot + availableSlots - 1;
        int centerIndex = rowStart + (availableSlots - 1) / 2;

        int placed = 0;
        int objectIndex = 0;

        if (objectCount % 2 == 1) {
            setItem(objects.get(objectIndex++), centerIndex);
            placed++;

            int step = 1;
            while (placed < objectCount) {
                int leftPos = centerIndex - step;
                if (leftPos >= rowStart) {
                    setItem(objects.get(objectIndex++), leftPos);
                    placed++;
                }

                int rightPos = centerIndex + step;
                if (placed < objectCount && rightPos <= rowEnd) {
                    setItem(objects.get(objectIndex++), rightPos);
                    placed++;
                }
                step++;
            }
        } else {
            int step = 1;
            while (placed < objectCount) {
                int leftPos = centerIndex - step;
                if (leftPos >= rowStart) {
                    setItem(objects.get(objectIndex++), leftPos);
                    placed++;
                    if (placed >= objectCount) break;
                }

                int rightPos = centerIndex + step;
                if (rightPos <= rowEnd) {
                    setItem(objects.get(objectIndex++), rightPos);
                    placed++;
                }
                step++;
            }
        }
    }
}