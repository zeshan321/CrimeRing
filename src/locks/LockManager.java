package locks;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class LockManager {

    private final int duration = 120;
    public HashMap<UUID, String> assign = new HashMap<>();
    public HashMap<String, Long> unlocked = new HashMap<>();
    public HashMap<UUID, String> lastOrder = new HashMap<>();
    public HashMap<String, String> locks = new HashMap<>();
    public HashMap<String, String> chests = new HashMap<>();
    public HashMap<UUID, String> arrows = new HashMap<>();

    public LockManager() {
        assign.clear();
        unlocked.clear();
        lastOrder.clear();
        locks.clear();
        chests.clear();

        // Load
        FileHandler imports = new FileHandler("plugins/CrimeRing/locks.yml");

        for (String data : imports.getKeys()) {
            String type = imports.getString(data + ".type");

            locks.put(data, type);
        }

        imports = new FileHandler("plugins/CrimeRing/chests.yml");

        for (String data : imports.getKeys()) {
            String type = imports.getString(data + ".type").toLowerCase();
            String trigger = imports.getString(data + ".trigger");

            chests.put(data + "-" + type, trigger);
        }
    }

    public void start() {
        Main.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Main.instance, () -> removeUnlocked(), 0, 1);
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
                Block block = Bukkit.getWorld(data[3]).getBlockAt(Integer.valueOf(data[0]), Integer.valueOf(data[1]), Integer.valueOf(data[2]));

                if (block.getType().toString().contains("DOOR")) {
                    BlockState state = block.getState();

                    Openable openable = (Openable) state.getData();
                    openable.setOpen(false);

                    state.setData((MaterialData) openable);
                    state.update();
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
