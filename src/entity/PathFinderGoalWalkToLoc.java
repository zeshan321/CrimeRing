package entity;

import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.PathEntity;
import net.minecraft.server.v1_10_R1.PathfinderGoal;
import org.bukkit.Location;

public class PathFinderGoalWalkToLoc extends PathfinderGoal {
    private EntityInsentient entity;
    private PathEntity path;
    private double speed;
    private Location target;

    public PathFinderGoalWalkToLoc(EntityInsentient entity, Location target, double speed) {
        this.entity = entity;
        this.speed = speed;
        this.target = target;

        // Allow bigger range
        this.entity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(entity.getBukkitEntity().getLocation().distance(target));
    }

    public boolean a() {
        this.path = this.entity.getNavigation().a(target.getBlockX(), target.getBlockY(), target.getBlockZ());
        if (this.path != null) {
            c();
        }
        return this.path != null;
    }

    public void c() {
        this.entity.onGround = true;

        this.entity.getNavigation().a(this.path, this.speed);
    }
}
