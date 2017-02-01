package customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.HashMap;

public class PlayerArrestedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Player cop;
    private long timeStamp;
    private HashMap<String, Integer> crimes;

    public PlayerArrestedEvent(Player player, Player cop, HashMap<String, Integer> crimes, long timeStamp) {
        this.player = player;
        this.cop = cop;
        this.crimes = crimes;
        this.timeStamp = timeStamp;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Player getCop() {
        return this.cop;
    }

    public HashMap<String, Integer> getCrimes() {
        return crimes;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
