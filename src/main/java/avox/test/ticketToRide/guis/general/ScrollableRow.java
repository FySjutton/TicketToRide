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

    private final boolean centered;

    public ScrollableRow(ActionManager actionManager, Inventory gui, int startSlot, int length, ArrayList<T> objectList, Component emptySlotName, boolean centered) {
        this.actionManager = actionManager;
        this.objectList = objectList;
        this.inventory = gui;
        this.startSlot = startSlot;
        this.length = length;
        this.emptySlotName = emptySlotName;
        this.centered = centered;

        updateRow();
    }

    public abstract ItemStack getSlotItem(T item);
    public abstract GuiAction getGuiAction(T object);

    private void updateRow() {
        clearRow();

        int objects = objectList.size();
        boolean hasBack = scroll > 0;
        boolean hasForward = scroll + (length - (hasBack ? 1 : 0)) < objects;

        int cardSlots = length - (hasBack ? 1 : 0) - (hasForward ? 1 : 0);

        if (objects <= cardSlots && centered) {
            distributeCentered(objectList);
            currentlyShown = new ArrayList<>(objectList);
            return;
        }

        int itemSlot = startSlot + (hasBack ? 1 : 0);

        if (hasBack) {
            actionManager.setAction(inventory,
                    GuiTools.format(new ItemStack(Material.ARROW), GuiTools.getYellow("Scroll back")),
                    startSlot,
                    GuiAction.ofClick(() -> {
                        if (scroll == 2) scroll = 0;
                        else scroll--;
                        updateRow();
                    })
            );
        }

        if (hasForward) {
            actionManager.setAction(inventory,
                    GuiTools.format(new ItemStack(Material.ARROW), GuiTools.getYellow("Scroll forward")),
                    startSlot + length - 1,
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

        ArrayList<T> visible = new ArrayList<>();
        for (int i = 0; i < cardSlots; i++) {
            int cardIndex = scroll + i;
            if (cardIndex < objects) {
                T object = objectList.get(cardIndex);
                setItem(itemSlot, object);
                visible.add(object);
            } else if (emptySlotName != null) {
                inventory.setItem(itemSlot, GuiTools.format(new ItemStack(Material.BARRIER), emptySlotName));
            }
            itemSlot++;
        }
        currentlyShown = visible;
    }

    private void clearRow() {
        for (int i = 0; i < length; i++) {
            actionManager.removeAction(startSlot + i);
            inventory.setItem(startSlot + i, null);
        }
    }

    private void setItem(int slot, T object) {
        if (slot < 0 || slot >= inventory.getSize()) return;

        GuiAction action = getGuiAction(object);
        ItemStack item = getSlotItem(object);

        if (action == null) {
            actionManager.removeAction(slot);
            inventory.setItem(slot, item);
        } else {
            actionManager.setAction(inventory, item, slot, action);
        }
    }

    private void distributeCentered(ArrayList<T> objects) {
        int count = objects.size();
        if (count == 0) return;

        int rowStart = startSlot;
        int rowEnd = startSlot + length - 1;
        int centerIndex = rowStart + (length - 1) / 2;

        int placed = 0;
        int index = 0;

        if (count % 2 == 1) {
            setItem(centerIndex, objects.get(index++));
            placed++;

            int step = 1;
            while (placed < count) {
                int left = centerIndex - step;
                if (left >= rowStart) {
                    setItem(left, objects.get(index++));
                    placed++;
                }

                int right = centerIndex + step;
                if (placed < count && right <= rowEnd) {
                    setItem(right, objects.get(index++));
                    placed++;
                }
                step++;
            }
        } else {
            int step = 1;
            while (placed < count) {
                int left = centerIndex - step;
                if (left >= rowStart) {
                    setItem(left, objects.get(index++));
                    placed++;
                    if (placed >= count) break;
                }

                int right = centerIndex + step;
                if (right <= rowEnd) {
                    setItem(right, objects.get(index++));
                    placed++;
                }
                step++;
            }
        }
    }
}
