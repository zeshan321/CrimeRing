package customevents;


import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LockpickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String lockType;
    private Block block;

    public LockpickEvent(Player player, String lockType, Block block) {
        this.player = player;
        this.lockType = lockType;
        this.block = block;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getLockType() {
        return this.lockType;
    }

    public Block getBlock() {
        return this.block;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
