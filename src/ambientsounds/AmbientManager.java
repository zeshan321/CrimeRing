package ambientsounds;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

public class AmbientManager {

    private final String[] sounds = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    public HashMap<UUID, AmbientObject> timer = new HashMap<>();
    public int time = 35;
    private Random random;

    public AmbientManager() {
        random = new Random();
        timer.clear();

        Main.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Main.instance, () -> {
            Iterator<UUID> iterator = timer.keySet().iterator();

            while (iterator.hasNext()) {
                UUID uuid = iterator.next();
                AmbientObject soundObject = timer.get(uuid);

                long timestamp = soundObject.timestamp;
                int seconds = soundObject.seconds;
                String type = soundObject.type;

                long secondsLeft = ((timestamp / 1000) + seconds) - (System.currentTimeMillis() / 1000);

                // Check if player is offline
                Player player = Bukkit.getPlayer(uuid);

                if (!player.isOnline()) {
                    iterator.remove();
                    continue;
                }

                // Check if player is in region
                if (!(Main.instance.actionDefaults.isInRegion(player, "city"))) {
                    iterator.remove();
                    continue;
                }

                if (secondsLeft <= 0) {
                    switch (type) {
                        case "CITY":
                            startCity(player);
                            timer.put(player.getUniqueId(), new AmbientObject(System.currentTimeMillis(), time, "CITY"));
                            break;
                    }
                }
            }
        }, 0, 20L);
    }

    public void startCity(Player player) {
        String track = "ambient.city." + sounds[random.nextInt(sounds.length)];
        Main.instance.actionDefaults.playCustomSound(player, track, "AMBIENT", player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), Float.MAX_VALUE, 0);
    }
}
