package customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;


public class PlayerTradeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String title;
    private final ItemStack item;

    /**
     * Constructor
     *
     * @param player player equipping an item
     * @param item   item that was equipped
     */
    public PlayerTradeEvent(Player player, String title, ItemStack item) {
        this.player = player;
        this.title = title;
        this.item = item;
    }

    /**
     * @return handlers for this event
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * @return plyer that equipped the item
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return item that was equipped
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * @return merchant title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return handlers for this event
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
