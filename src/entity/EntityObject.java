package entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EntityObject {

    public Player owner;
    public Entity entity;
    public boolean hidden;

    public EntityObject(Player owner, Entity entity, boolean hidden) {
        this.owner = owner;
        this.entity = entity;
        this.hidden = hidden;
    }
}
