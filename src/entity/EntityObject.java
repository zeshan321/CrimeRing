package entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EntityObject {

    public Player owner;
    public Entity entity;
    public boolean hidden;
    public UUID entityUUID;

    public int lastX = 0;
    public int lastY = 0;
    public int lastZ = 0;

    public EntityObject(Player owner, Entity entity, boolean hidden, UUID entityUUID) {
        this.owner = owner;
        this.entity = entity;
        this.hidden = hidden;
        this.entityUUID = entityUUID;
    }
}
