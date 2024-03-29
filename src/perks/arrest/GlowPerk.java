package perks.arrest;

import com.zeshanaslam.crimering.Main;
import customevents.PlayerArrestedEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import raids.PartyAPI;
import raids.PartyObject;

public class GlowPerk implements Listener {

    private final Main plugin;
    private final CopUtil copUtil;


    public GlowPerk(Main plugin) {
        this.plugin = plugin;
        this.copUtil = new CopUtil();
    }

    // on catch
    @EventHandler
    public void onCatch(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player cop = (Player) event.getDamager();
            Player player = (Player) event.getEntity();

            if (plugin.actionDefaults.isInRaid(player)) {
                return;
            }

            PartyObject partyObject = new PartyAPI().getParty(cop);
            if (partyObject != null) {
                if (partyObject.hasMember(player)) {
                    return;
                }
            }

            if (copUtil.isCop(cop)) {
                if (cop.getInventory().getItemInMainHand() != null) {

                    ItemStack item = cop.getInventory().getItemInMainHand();
                    if (item.getTypeId() == 293 && item.getDurability() == 494) {
                        event.setCancelled(true);

                        if (copUtil.isWanted(player)) {
                            double maxHP = player.getHealthScale() * .40;

                            if (player.getHealth() <= maxHP) {
                                TimerObject timerObject = Main.instance.perkManager.timer.get(player.getUniqueId());

                                // Remove from timer
                                Main.instance.perkManager.timer.remove(player.getUniqueId());
                                Main.instance.entityGlowHelper.updateGlows(player);

                                Main.instance.getServer().getPluginManager().callEvent(new PlayerArrestedEvent(player, cop, timerObject.crimes, timerObject.timestamp));
                            } else {
                                cop.sendMessage(ChatColor.RED + player.getName() + "'s HP is too high!");
                            }
                        } else {
                            cop.sendMessage(ChatColor.RED + "You cannot arrest a player that is not wanted!");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCopKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Entity killed = event.getEntity();
        Player killer = event.getEntity().getKiller();

        if (Main.instance.mythicAPI.isMythicMob(killed) && Main.instance.mythicAPI.getMythicMobInstance(killed).getType().getInternalName().toLowerCase().contains("cop")) {
            copUtil.logCrime(killer, PerkManager.Crime.COPKILL);
            return;
        }

        if (killed instanceof Player) {
            if (copUtil.isCop((Player) killed)) {
                copUtil.logCrime(killer, PerkManager.Crime.COPKILL);
                return;
            }
            copUtil.logCrime(killer, PerkManager.Crime.PLAYERKILL);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player player = (Player) event.getDamager();

            copUtil.logCrime(player, PerkManager.Crime.ASSAULT);
        }
    }
}
