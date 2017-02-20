package events;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import me.robin.battlelevels.events.PlayerLevelUpEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.inventivetalent.bossbar.BossBar;
import org.inventivetalent.bossbar.BossBarAPI;

public class PlayerEvents implements Listener {

    private final Main plugin;

    public PlayerEvents(Main plugin) {
        this.plugin = plugin;
    }

    // Create default player file if new player
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return;
        }

        FileHandler fileHandler = new FileHandler(("plugins/CrimeRing/player/" + player.getUniqueId().toString()));
        fileHandler.set("in-name", player.getName());
        fileHandler.set("tutorial", false);
        fileHandler.set("skill-points", 0);

        fileHandler.save();
    }

    // Every 10 levels give player skill point
    @EventHandler
    public void onLevel(PlayerLevelUpEvent event) {
        Player player = event.getPlayer();

        if (event.getLevelTo() % 10 == 0) {
            FileHandler fileHandler = new FileHandler(("plugins/CrimeRing/player/" + player.getUniqueId().toString()));
            fileHandler.increment("skill-points");

            fileHandler.save();

            BossBar bossBar = BossBarAPI.addBar(player, // The receiver of the BossBar
                    new TextComponent(ChatColor.AQUA + "You now have " + ChatColor.GREEN + fileHandler.getInteger("skill-points") + ChatColor.AQUA + " skill points!"),
                    BossBarAPI.Color.YELLOW,
                    BossBarAPI.Style.NOTCHED_20,
                    1.0f,
                    35,
                    2);

        }
    }

    // Set attributes on join
    @EventHandler
    public void onJoinAtt(PlayerJoinEvent event) {
        plugin.actionDefaults.updateAttribute(event.getPlayer());
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        if (event.getItem().getTypeId() == 392 || event.getItem().getTypeId() == 391) {
            event.setCancelled(true);
        }
    }

    // On death
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().getName().equalsIgnoreCase("world")) {
            Location location = new Location(player.getWorld(), -295, 63, -77, (float) 155.504, (float) 0.75);
            event.setRespawnLocation(location);
        }
    }
}
