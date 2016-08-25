package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Iterator;
import java.util.UUID;

public class ScriptListener implements Listener {

    private final Main plugin;

    public ScriptListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (plugin.flag.contains(event.getPlayer().getUniqueId().toString() + "-" + "stopmove")) {

            Location from = event.getFrom();
            if (from.getZ() != event.getTo().getZ() && from.getX() != event.getTo().getX()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Iterator<String> flagIter = plugin.flag.iterator();
        while (flagIter.hasNext()) {
            String flag = flagIter.next();

            if (flag.startsWith(player.getUniqueId().toString())) {
                flagIter.remove();
            }
        }

        Iterator<UUID> listenerIter = Main.instance.listeners.rowKeySet().iterator();
        while (listenerIter.hasNext()) {
            if (listenerIter.next() == player.getUniqueId()) {
                listenerIter.remove();
            }
        }
    }
}
