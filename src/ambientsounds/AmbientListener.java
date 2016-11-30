package ambientsounds;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AmbientListener implements Listener {

    private final Main plugin;
    private final int time;

    public AmbientListener(Main plugin) {
        this.plugin = plugin;
        this.time = plugin.ambientManager.time;
    }

    @EventHandler
    public void onEnterRegion(RegionEnterEvent event) {
        Player player = event.getPlayer();

        if (event.getRegion().getId().equals("city")) {
            plugin.ambientManager.timer.put(player.getUniqueId(), new AmbientObject(System.currentTimeMillis(), time, "CITY"));
            plugin.ambientManager.startCity(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.actionDefaults.isInRegion(player, "city")) {
            plugin.ambientManager.timer.put(player.getUniqueId(), new AmbientObject(System.currentTimeMillis(), time, "CITY"));
            plugin.ambientManager.startCity(player);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        plugin.ambientManager.timer.remove(event.getPlayer().getUniqueId());
    }
}
