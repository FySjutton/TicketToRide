package avox.test.ticketToRide.guis.general;

import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.GuiTools;
import avox.test.ticketToRide.guis.InventoryGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static avox.test.ticketToRide.guis.GuiTools.colorize;
import static avox.test.ticketToRide.guis.GuiTools.getYellow;

public class SelectObjectGui<T> extends InventoryGui {
    private ArrayList<ObjectEntry> options;
    private Consumer<ObjectEntry> action;

    private int page = 1;
    private int pages;

    private Consumer<ArrayList<ObjectEntry>> finalAction; // Multiselect only
    private boolean multiselect;

    public SelectObjectGui(Player player, Component name) {
        super(player, 36, name);
    }

    public void init(ArrayList<ObjectEntry> options, Consumer<ObjectEntry> resultAction) {
        this.action = resultAction;
        init(options, false);
    }

    public void initMultiselect(ArrayList<ObjectEntry> options, Consumer<ObjectEntry> resultAction, Consumer<ArrayList<ObjectEntry>> finalAction) {
        this.finalAction = finalAction;
        this.action = resultAction;
        init(options, true);
    }

    private void init(ArrayList<ObjectEntry> options, boolean multiselect) {
        this.options = options;
        pages = (int) Math.ceil((double) options.size() / 27);
        this.multiselect = multiselect;
        updateInventory();
    }

    private void updateInventory() {
        gui.clear();
        actionManager.clear();
        List<ObjectEntry> pageMaps = options.subList(page * 27 - 27, Math.min(page * 27, options.size()));
        int slot = 0;
        for (ObjectEntry map : pageMaps) {
            ItemStack item = GuiTools.format(GuiTools.createHead(map.texture), map.name, List.of(map.error ? map.message : map.description));
            int finalSlot = slot;
            actionManager.setSlot(gui, item, slot, GuiAction.ofClick(() -> {
                if (!map.error) {
                    if (multiselect) {
                        map.selected = !map.selected;
                    } else {
                        gui.clear(finalSlot);
                    }
                    action.accept(map);
                    updateInventory();
                }
            }));
            slot++;
        }

        if (multiselect) {
            ItemStack finishButton = GuiTools.format(
                new ItemStack(Material.GREEN_CONCRETE),
                colorize(Component.text("Finished"), NamedTextColor.GREEN),
                List.of(colorize(Component.text("Click to invite all selected players!"), NamedTextColor.GRAY))
            );

            actionManager.setSlot(gui, finishButton, 31, GuiAction.ofClick(() -> finalAction.accept(new ArrayList<>(options.stream().filter(option -> option.selected).toList()))));
        }

        if (pages > 1 && pages != page) {
            ItemStack arrow = GuiTools.format(
                new ItemStack(Material.ARROW),
                getYellow("Next Page"),
                List.of(colorize(Component.text("Page " + (page + 1) + "/" + pages), NamedTextColor.GRAY))
            );

            actionManager.setSlot(gui, arrow, 35, GuiAction.ofClick(() -> {
                gui.clear(35);
                page++;
                updateInventory();
            }));
        }
        if (page > 1) {
            ItemStack arrow = GuiTools.format(
                    new ItemStack(Material.ARROW),
                    getYellow("Previous Page"),
                    List.of(colorize(Component.text("Page " + (page - 1) + "/" + pages), NamedTextColor.GRAY))
            );

            actionManager.setSlot(gui, arrow, 27, GuiAction.ofClick(() -> {
                gui.clear(27);
                page--;
                updateInventory();
            }));
        }
    }

    public class ObjectEntry {
        public Component name;
        public Component description;
        public String texture;
        public T element;

        public boolean error = false;
        public Component message;

        // Used only for multiselect
        public boolean selected;

        public ObjectEntry(Component name, Component description, String texture, T element) {
            this(name, description, texture, element, false);
        }

        public ObjectEntry(Component name, Component description, String texture, T element, boolean selected) {
            this(name, description, texture, element, selected, false, null);
        }

        public ObjectEntry(Component name, Component description, String texture, T element, boolean selected, boolean error, Component message) {
            this.name = name;
            this.texture = texture;
            this.description = description;
            this.element = element;
            this.selected = selected;
            this.error = error;
            this.message = message;
        }
    }
}
