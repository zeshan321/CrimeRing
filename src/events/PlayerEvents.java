package events;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import me.robin.battlelevels.events.PlayerLevelUpEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEvents implements Listener {

    private final Main plugin;

    public PlayerEvents(Main plugin) {
        this.plugin = plugin;
    }

    // Create default player file if new player
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return;
        }

        FileHandler fileHandler = new FileHandler(("plugins/CrimeRing/player/" + player.getUniqueId().toString()));
        fileHandler.set("tutorial", false);
        fileHandler.set("skill-points", 0);

        fileHandler.save();
    }

    // Every 10 levels give player skill point
    @EventHandler
    public void onLevel(PlayerLevelUpEvent event) {
        Player player = event.getPlayer();

        if (event.getLevelTo() % 5 == 0) {
            FileHandler fileHandler = new FileHandler(("plugins/CrimeRing/player/" + player.getUniqueId().toString()));
            fileHandler.increment("skill-points");

            fileHandler.save();
        }
    }
}
