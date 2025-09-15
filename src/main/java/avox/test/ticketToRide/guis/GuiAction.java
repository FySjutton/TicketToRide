package avox.test.ticketToRide.guis;

public class GuiAction {
    public final Runnable clickAction;
    public final Runnable holdAction;

    private GuiAction(Runnable clickAction, Runnable holdAction) {
        this.clickAction = clickAction;
        this.holdAction = holdAction;
    }

    public static GuiAction ofClick(Runnable clickAction) {
        return new GuiAction(clickAction, null);
    }

    public static GuiAction ofHold(Runnable holdAction) {
        return new GuiAction(null, holdAction);
    }

    public static GuiAction of(Runnable clickAction, Runnable holdAction) {
        return new GuiAction(clickAction, holdAction);
    }
}
