package locks;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.*;

public class LockManager {

    public HashMap<UUID, String> assign = new HashMap<>();
    public HashMap<String, Long> unlocked = new HashMap<>();
    public HashMap<UUID, String> lastOrder = new HashMap<>();

    private final int duration = 30;

    public LockManager() {
        assign.clear();
        unlocked.clear();
        lastOrder.clear();

        Main.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Main.instance, new Runnable() {
            @Override
            public void run() {
                removeUnlocked();
            }
        }, 0, 1);
    }

    public void removeUnlocked() {
        Iterator<String> iterator = unlocked.keySet().iterator();
        while(iterator.hasNext()) {
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
}
