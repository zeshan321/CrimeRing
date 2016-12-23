package locks;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class LockManager {

    private final int duration = 30;
    public HashMap<UUID, String> assign = new HashMap<>();
    public HashMap<String, Long> unlocked = new HashMap<>();
    public HashMap<UUID, String> lastOrder = new HashMap<>();
    public HashMap<String, String> locks = new HashMap<>();

    public LockManager() {
        assign.clear();
        unlocked.clear();
        lastOrder.clear();
        locks.clear();

        // Load
        FileHandler imports = new FileHandler("plugins/CrimeRing/locks.yml");

        for (String data : imports.getKeys()) {
            String type = imports.getString(data + ".type");

            locks.put(data, type);
        }
    }

    public void start() {
        Main.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Main.instance, new Runnable() {
            @Override
            public void run() {
                removeUnlocked();
            }
        }, 0, 1);
    }

    public void removeUnlocked() {
        Iterator<String> iterator = unlocked.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            long start = unlocked.get(key);

            long secondsLeft = ((start / 1000) + duration) - (System.currentTimeMillis() / 1000);
            if (secondsLeft <= 0) {
                iterator.remove();

                String[] data = key.split(" ");
                Block block = Bukkit.getWorld(data[4]).getBlockAt(Integer.valueOf(data[1]), Integer.valueOf(data[2]), Integer.valueOf(data[3]));

                if (block.getType().toString().contains("DOOR")) {
                    block.setData((byte) 0);
                }
            }
        }
    }

    public void addLock(String key, String value) {
        locks.put(key, value);

        FileHandler imports = new FileHandler("plugins/CrimeRing/locks.yml");

        imports.set(key + ".type", value);
        imports.save();
    }

    public void removeLock(String key) {
        locks.remove(key);

        FileHandler imports = new FileHandler("plugins/CrimeRing/locks.yml");

        imports.remove(key);
        imports.save();
    }
}
