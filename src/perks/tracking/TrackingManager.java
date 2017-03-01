package perks.tracking;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class TrackingManager {

    public HashMap<UUID, TrackObject> tracking = new HashMap<>();

    public TrackingManager() {
        Bukkit.getScheduler().runTaskTimer(Main.instance, () -> {
            Iterator<UUID> iterator = tracking.keySet().iterator();
            while (iterator.hasNext()) {
                UUID uuid = iterator.next();
                TrackObject trackObject = tracking.get(uuid);

                if (Bukkit.getPlayer(trackObject.tracking) != null) {
                    Player player = Bukkit.getPlayer(trackObject.tracking);
                    if (player.isOnline()) {
                        if (player.getLocation().getWorld().getName().equalsIgnoreCase("world")) {
                            trackObject.lastLocation = player.getLocation();
                            iterator.remove();

                            tracking.put(uuid, trackObject);
                        }
                    }
                }

                if (Bukkit.getPlayer(uuid) != null) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (!player.isOnline()) {
                        iterator.remove();
                        return;
                    }

                    int base = 250;
                    if (Main.instance.cooldownManager.perksUtil.hasPermission(player, "CR.perk.tracking.1")) {
                        base = 225;
                    }

                    if (Main.instance.cooldownManager.perksUtil.hasPermission(player, "CR.perk.tracking.2")) {
                        base = 200;
                    }

                    if (Main.instance.cooldownManager.perksUtil.hasPermission(player, "CR.perk.tracking.3")) {
                        base = 175;
                    }

                    player.setCompassTarget(Main.instance.actionDefaults.getRandomLocations(trackObject.lastLocation, base, 1, true).get(0));
                }
            }
        }, 0, 100L);
    }
}
