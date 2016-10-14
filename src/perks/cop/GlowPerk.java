package perks.cop;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.inventivetalent.glow.GlowAPI;
import perks.TimerObject;

public class GlowPerk implements Listener {

    private final Main plugin;

    private final String glowPerkPerm = "CR.glow";
    private final int glowPerkTimer = 5 * 60;

    public GlowPerk(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCopKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Entity killed = event.getEntity();
        Player killer = event.getEntity().getKiller();

        if (killed.hasPermission(glowPerkPerm) || Main.instance.mythicAPI.isMythicMob(killed) && Main.instance.mythicAPI.getMythicMobInstance(killed).getType().getInternalName().startsWith("Cop")) {
            GlowAPI.setGlowing(killer, GlowAPI.Color.RED, plugin.perkManager.copUtil.getCops());

            // Add to timer
            plugin.perkManager.timer.put(killer.getUniqueId(), new TimerObject(System.currentTimeMillis(), glowPerkTimer, "GLOW"));

            killer.sendMessage(ChatColor.RED + "You are now wanted by the police!");
        }
    }

    // Reapply glows on join
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.perkManager.timer.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                GlowAPI.setGlowing(player, GlowAPI.Color.RED, plugin.perkManager.copUtil.getCops());
            }, 1L);
        }
    }
}
