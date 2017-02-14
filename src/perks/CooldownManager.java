package perks;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;

import java.util.*;

public class CooldownManager {

    private Multimap<UUID, CooldownObject> cooldowns = ArrayListMultimap.create();
    public PerksUtil perksUtil = new PerksUtil();

    public CooldownManager() {
        Main.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Main.instance, () -> {
            Set keySet = cooldowns.keySet();
            Iterator keyIterator = keySet.iterator();

            while (keyIterator.hasNext()) {
                UUID key = (UUID) keyIterator.next();
                Collection<CooldownObject> values = cooldowns.get(key);

                Iterator<CooldownObject> coolIter = values.iterator();
                while (coolIter.hasNext()) {
                    CooldownObject object = coolIter.next();

                    long secondsLeft = ((object.timestamp / 1000) + object.seconds) - (System.currentTimeMillis() / 1000);

                    if (secondsLeft <= 0) {
                        coolIter.remove();
                    }
                }

                if (values.isEmpty()) {
                    keyIterator.remove();
                }
            }
        }, 0, 20L);
    }

    public void addCooldown(Player player, String type, int seconds) {
        cooldowns.put(player.getUniqueId(), new CooldownObject(type, seconds, System.currentTimeMillis()));
    }

    public long isInCooldown(Player player, String type) {
        for (CooldownObject object: cooldowns.get(player.getUniqueId())) {
            if (object.type.equals(type)) {
                long secondsLeft = ((object.timestamp / 1000) + object.seconds) - (System.currentTimeMillis() / 1000);
                return secondsLeft;
            }
        }

        return 0;
    }
}
