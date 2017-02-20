package perks.arrest;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
                Player player = Bukkit.getPlayer(uuid);
                TimerObject timerObject = timer.get(uuid);

                long timestamp = timerObject.timestamp;
                int seconds = timerObject.seconds;
                long secondsLeft = ((timestamp / 1000) + seconds) - (System.currentTimeMillis() / 1000);

                if (player != null) {

                    // Check if cop nearby
                    if (player.isOnline()) {
                        if (copUtil.isCopNear(player)) {
                            iterator.remove();

                            timerObject.seconds = timerObject.seconds + 1;
                            timer.put(player.getUniqueId(), timerObject);

                            Main.instance.actionDefaults.sendActionBar(player, ChatColor.GOLD + "Status: " + ChatColor.RED + "Wanted");
                            continue;
                        }
                    }
                }

                if (secondsLeft <= 0) {
                    if (player != null) {

                        if (!player.isOnline()) {
                            iterator.remove();
                            Main.instance.actionDefaults.removePotionEffect(player, "GLOWING");
                            continue;
                        }


                        Main.instance.actionDefaults.sendActionBar(player, ChatColor.GOLD + "Status: " + ChatColor.GREEN + "Evaded");
                        Main.instance.actionDefaults.removePotionEffect(player, "GLOWING");
                        iterator.remove();
                    } else {
                        iterator.remove();
                    }
                } else {
                    if (player != null && player.isOnline()) {
                        Main.instance.actionDefaults.sendActionBar(player, ChatColor.GOLD + "Status: " + ChatColor.RED + "Wanted");
                    }
                }
            }
        }, 0, 20L);
    }

    public enum Crime {
        COPKILL, PLAYERKILL, LOCKPICK, ASSAULT
    }
}
