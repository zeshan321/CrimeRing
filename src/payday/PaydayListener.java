package payday;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import perks.PerksUtil;

public class PaydayListener implements Listener {

    private final Main plugin;

    public PaydayListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (new PerksUtil().hasPermission(player, "CR.cop")) {
            plugin.paydayManager.players.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.paydayManager.players.containsKey(player.getUniqueId())) {
            plugin.paydayManager.players.remove(player.getUniqueId());
        }
    }
}
