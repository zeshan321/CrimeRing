package events;

import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import raids.PartyAPI;
import raids.PartyObject;

import java.util.List;


public class BasicEvents implements Listener {

    private final Main plugin;

    public BasicEvents(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginsView(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String cmd = event.getMessage().toLowerCase();

        if (cmd.startsWith("/bukkit:pl") || cmd.startsWith("/bukkit:plugin") || cmd.startsWith("/bukkit:plugins") || cmd.startsWith("/pl") || cmd.startsWith("/plugin") || cmd.startsWith("/plugins")) {
            player.sendMessage(ChatColor.WHITE + "Plugins (1): " + ChatColor.GREEN + "CrimeRing");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageItem(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockedCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String label = event.getMessage().split(" ")[0].toLowerCase();

        if (label.startsWith("/reload")) {
            event.setCancelled(true);
        }

        if (player.isOp()) {
            return;
        }

        if (plugin.getConfig().getStringList("Blocked-cmds").contains(label)) {
            player.sendMessage(ChatColor.RED + "You do not have access to this command!");
            event.setCancelled(true);
            return;
        }

        if (plugin.raidManager.raids.containsKey(player) && plugin.getConfig().getStringList("Blocked-cmds-raid").contains(label)) {
            player.sendMessage(ChatColor.RED + "You can not use that command while in a raid!");
            event.setCancelled(true);
            return;
        }

        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);
        if (party != null) {
            if (party.getMembers().contains(player)) {
                if (plugin.raidManager.raids.containsKey(player) && plugin.getConfig().getStringList("Blocked-cmds-raid").contains(label)) {
                    player.sendMessage(ChatColor.RED + "You can not use that command while in a raid!");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(BlockBreakEvent event) {
        List<Integer> remove = plugin.getConfig().getIntegerList("Blocked-items-drop");

        if (remove.contains(event.getBlock().getTypeId())) {
            event.setCancelled(true);
            event.getBlock().breakNaturally();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getClickedBlock() == null) {
            return;
        }

        if (player.isOp()) {
            return;
        }

        if (plugin.getConfig().getIntegerList("Blocked-items-interact").contains(event.getClickedBlock().getTypeId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
            event.getWorld().setWeatherDuration(0);
        }
    }

    @EventHandler
    public void onCrackShotBug(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();

            if (projectile.getShooter() instanceof Player) {
                Player damaged = (Player) event.getEntity();
                Player shooter = (Player) projectile.getShooter();

                if (damaged.getName().equals(shooter.getName())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
