package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ScriptListener implements Listener {

    private final Main plugin;

    public ScriptListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (plugin.flag.contains(event.getPlayer().getUniqueId().toString() + "-" + "stopmove")) {
            event.setCancelled(true);
        }
    }
}
