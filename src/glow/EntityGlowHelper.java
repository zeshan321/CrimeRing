package glow;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import packets.EntityFlagFilter;
import perks.arrest.CopUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EntityGlowHelper {

    private EntityFlagFilter glowFilter;
    private HashMap<UUID, List<UUID>> glowing = new HashMap<>();

    public EntityGlowHelper() {
        glowFilter = new EntityFlagFilter(Main.instance) {
            @Override
            protected int filterFlag(Player observer, Entity observed, int flagValues) {
                if (observed == null || observed.getUniqueId() == null) {
                    return flagValues;
                }

                // Check if player is wanted
                CopUtil copUtil = new CopUtil();
                if (observed instanceof Player) {
                    Player player = (Player) observed;
                    if (copUtil.isWanted(player) && copUtil.isCop(observer)) {
                        return flagValues | ENTITY_GLOWING;
                    }
                }

                // Other
                if (glowing.containsKey(observed.getUniqueId())) {
                    List<UUID> viewing = glowing.get(observed.getUniqueId());

                    if (viewing.isEmpty()) {
                        glowing.remove(observed.getUniqueId());
                        return flagValues;
                    }

                    if (viewing.contains(observer.getUniqueId())) {
                        return flagValues | ENTITY_GLOWING;
                    }
                }

                return flagValues;
            }
        };
        glowFilter.register();
    }

    public void addGlow(Player observer, Entity observed) {
        if (glowing.containsKey(observed.getUniqueId())) {
            List<UUID> viewing = glowing.get(observed.getUniqueId());

            if (!viewing.contains(observer.getUniqueId())) {
                viewing.add(observer.getUniqueId());

                glowing.put(observed.getUniqueId(), viewing);
            }
        } else {
            List<UUID> viewing = new ArrayList<>();
            viewing.add(observer.getUniqueId());

            glowing.put(observed.getUniqueId(), viewing);
        }

        glowFilter.updateEntity(observed);
    }

    public void hideGlow(Player observer, Entity observed) {
        if (glowing.containsKey(observed.getUniqueId())) {
            List<UUID> viewing = glowing.get(observed.getUniqueId());

            if (viewing.contains(observer.getUniqueId())) {
                viewing.remove(observer.getUniqueId());

                glowing.put(observed.getUniqueId(), viewing);
                glowFilter.updateEntity(observed);
            }
        }
    }

    public void removeGlow(Entity observed) {
        if (glowing.containsKey(observed.getUniqueId())) {
            glowing.remove(observed.getUniqueId());

            glowFilter.updateEntity(observed);
        }
    }

    public void updateGlows(Entity observed) {
        glowFilter.updateEntity(observed);
    }
}
