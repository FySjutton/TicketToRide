package avox.test.ticketToRide.guis.general;

import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.util.GuiTools;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public abstract class ScrollableRow<T> {
    private final ActionManager actionManager;
    private final ArrayList<T> objectList;
    private final Inventory inventory;
    private final int startSlot;
    private final int length;
    private final Component emptySlotName;

    public ArrayList<T> currentlyShown;
    private int scroll = 0;

    public ScrollableRow(ActionManager actionManager, Inventory gui, int startSlot, int length, ArrayList<T> objectList, Component emptySlotName) {
        this.actionManager = actionManager;
        this.objectList = objectList;
        this.inventory = gui;
        this.startSlot = startSlot;
        this.length = length;
        this.emptySlotName = emptySlotName;

        updateRow();
    }

    public abstract ItemStack getSlotItem(T item);

    private void updateRow() {
        int objects = objectList.size();

        for (int i = 0; i <= length; i++) {
            actionManager.removeAction(startSlot + i);
            inventory.setItem(startSlot + i, null);
        }

        boolean hasBack = scroll > 0;
        boolean hasForward = scroll + (length - (hasBack ? 1 : 0)) < objects;

        int cardSlots = length - (hasBack ? 1 : 0) - (hasForward ? 1 : 0);
        int itemSlot = startSlot + (hasBack ? 1 : 0);

        if (hasBack) {
            actionManager.addAction(inventory, GuiTools.format(new ItemStack(Material.ARROW), GuiTools.getYellow("Scroll back")), startSlot,
                    GuiAction.ofClick(() -> {
                        if (scroll == 2) scroll = 0;
                        else scroll--;
                        updateRow();
                    })
            );
        }

        if (hasForward) {
            actionManager.addAction(inventory, GuiTools.format(new ItemStack(Material.ARROW), GuiTools.getYellow("Scroll forward")), startSlot + length - 1,
                GuiAction.ofClick(() -> {
                    if (scroll == 0 && objects > length - 1) {
                        scroll = 2;
                    } else {
                        scroll++;
                    }
                    updateRow();
                })
            );
        }

        for (int i = 0; i < cardSlots; i++) {
            actionManager.removeAction(itemSlot);
            int cardIndex = scroll + i;
            if (cardIndex < objects) {
                T object = objectList.get(cardIndex);
                inventory.setItem(itemSlot, getSlotItem(object));
            } else if (emptySlotName != null) {
                inventory.setItem(itemSlot, GuiTools.format(new ItemStack(Material.BARRIER), emptySlotName));
            }
            itemSlot++;
        }

        ArrayList<T> visibleObjects = new ArrayList<>();
        for (int i = 0; i < cardSlots; i++) {
            int idx = scroll + i;
            if (idx < objects) visibleObjects.add(objectList.get(idx));
        }
        currentlyShown = visibleObjects;
    }
}
