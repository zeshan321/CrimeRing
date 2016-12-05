package detection;

import org.bukkit.entity.Entity;

public class EntityDetectionObject {

    public Entity entity;
    public String script;

    public EntityDetectionObject(Entity entity, String script) {
        this.entity = entity;
        this.script = script;
    }

}
