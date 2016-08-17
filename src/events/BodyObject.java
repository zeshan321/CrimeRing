package events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BodyObject {

    public Player owner;
    public Entity killer;
    public long time;
    public Location loc;
    public Inventory inventory;

    public BodyObject(Player owner, Entity killer, long time, Location loc, Inventory inventory) {
        this.owner = owner;
        this.killer = killer;
        this.time = time;
        this.loc = loc;
        this.inventory = inventory;
    }
}
