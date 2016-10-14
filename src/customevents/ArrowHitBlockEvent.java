package customevents;


import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ArrowHitBlockEvent extends BlockEvent {

    private static final HandlerList handlers = new HandlerList();
    private Arrow arrow;
    private ProjectileSource shooter;

    public ArrowHitBlockEvent(Arrow arrow, Block block, ProjectileSource shooter) {
        super(block);

        this.arrow = arrow;
        this.shooter = shooter;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Arrow getArrow() {
        return this.arrow;
    }

    public ProjectileSource getShooter() {
        return this.shooter;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
