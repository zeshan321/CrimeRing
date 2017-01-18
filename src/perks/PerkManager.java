package perks;

import com.zeshanaslam.crimering.Main;
import de.Herbystar.TTA.TTA_Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import perks.cop.CopUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class PerkManager {

    public HashMap<UUID, TimerObject> timer = new HashMap<>();

    // Utils
    public CopUtil copUtil;

    public PerkManager() {
        timer.clear();

        copUtil = new CopUtil();

        Main.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Main.instance, () -> {
            Iterator<UUID> iterator = timer.keySet().iterator();

            while (iterator.hasNext()) {
                UUID uuid = iterator.next();
                TimerObject timerObject = timer.get(uuid);

                long timestamp = timerObject.timestamp;
                int seconds = timerObject.seconds;
                String type = timerObject.type;

                long secondsLeft = ((timestamp / 1000) + seconds) - (System.currentTimeMillis() / 1000);

                if (secondsLeft <= 0) {
                    // Check if player is offline
                    Player player = Bukkit.getPlayer(uuid);

                    if (!player.isOnline()) {
                        iterator.remove();
                        continue;
                    }

                    switch (type) {
                        case "GLOW":
                            TTA_Methods.removeEntityGlow(player);
                            player.sendMessage(ChatColor.GOLD + "You are no longer wanted by the police!");
                            break;

                        default:
                            System.out.println("Unknown perk timer!");
                            break;
                    }

                    iterator.remove();
                }
            }
        }, 0, 20L);
    }
}
