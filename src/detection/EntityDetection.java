package detection;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.bossbar.BossBar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class EntityDetection {

    private List<EntityDetectionObject> list = new ArrayList<>();
    private List<UUID> bars = new ArrayList<>();
    private List<String> remove = new ArrayList<>();

    public EntityDetection() {
        new BukkitRunnable() {
            public void run() {
                bars.clear();
                remove.clear();

                Iterator<EntityDetectionObject> iterator = list.iterator();

                while (iterator.hasNext()) {
                    EntityDetectionObject object = iterator.next();

                    if (object.entity.isDead() || remove.contains(object.script)) {
                        iterator.remove();
                        continue;
                    }

                    List<Player> players = Main.instance.actionDefaults.getPlayersInCone(object.entity, 90, 16);

                    for (Player player : players) {
                        if (bars.contains(player.getUniqueId())) {
                            continue;
                        }

                        bars.add(player.getUniqueId());

                        BossBar bar = Main.instance.actionDefaults.getBossBar(player, "PROGRESS");
                        if (bar == null) {
                            Main.instance.actionDefaults.createBossBar(player, "Detection", "RED", "PROGRESS", 0);
                        } else {
                            Main.instance.actionDefaults.addBossBarProgress(bar, 0.4f);

                            if (bar.getProgress() >= 1.0f) {
                                Main.instance.actionDefaults.runScript(player, object.script);
                                bar.removePlayer(player);
                                iterator.remove();
                                remove.add(object.script);
                                break;
                            }
                        }
                    }
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    BossBar bar = Main.instance.actionDefaults.getBossBar(player, "PROGRESS");

                    if (bar != null && !bars.contains(player.getUniqueId())) {
                        if (bar.getProgress() <= 0.0f) {
                            bar.removePlayer(player);
                        } else {
                            bar.setProgress(bar.getProgress() - 0.3f);
                        }
                    }
                }
            }
        }.runTaskTimer(Main.instance, 0L, 20L);
    }

    public void addEntity(Entity entity, String script) {
        list.add(new EntityDetectionObject(entity, script));
    }
}
