package radio;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class RadioManager {

    public List<RadioObject> songs = new ArrayList<>();
    public List<UUID> listen = new ArrayList<>();
    public RadioObject currentSong = null;

    public RadioManager() {
        Main.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Main.instance, () -> {
            if (currentSong == null) {
                if (!songs.isEmpty()) {
                    currentSong = songs.get(new Random().nextInt(songs.size()));
                    currentSong.timestamp = System.currentTimeMillis() + 25;
                }
            } else {
                long secondsLeft = ((currentSong.timestamp / 1000) + currentSong.time) - (System.currentTimeMillis() / 1000);

                if (secondsLeft <= 0) {
                    Iterator<UUID> iterable = listen.iterator();
                    while (iterable.hasNext()) {
                        Player player = Bukkit.getPlayer(iterable.next());

                        if (player == null || !player.isOnline()) {
                            iterable.remove();
                        }


                        Main.instance.actionDefaults.audioPlayToPlayer(player, "stop");
                    }

                    currentSong = null;
                }
            }
        }, 0, 20L);
    }

    public void load() {
        songs.clear();

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/radio.yml");

        for (String s : fileHandler.getStringList("radio")) {
            String[] data = s.split(" ");

            songs.add(new RadioObject(data[0], Integer.valueOf(data[1]), System.currentTimeMillis()));
        }
    }

    public void addPlayer(Player player) {
        listen.add(player.getUniqueId());

        Main.instance.actionDefaults.audioPlayToPlayer(player, currentSong.name);
    }

    public void removePlayer(Player player) {
        listen.remove(player.getUniqueId());
        Main.instance.actionDefaults.audioPlayToPlayer(player, "stop");
    }
}
