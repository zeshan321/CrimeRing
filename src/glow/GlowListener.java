package glow;

import com.zeshanaslam.crimering.Main;
import me.libraryaddict.disguise.events.DisguiseEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GlowListener implements Listener {

    private final Main plugin;

    public GlowListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        plugin.entityGlowHelper.removeGlow(entity);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.entityGlowHelper.removeGlow(player);
    }

    @EventHandler
    public void onDisguise(DisguiseEvent event) {
        Entity entity = event.getEntity();

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.entityGlowHelper.updateGlows(entity);
            }
        }, 40L);
    }
}
