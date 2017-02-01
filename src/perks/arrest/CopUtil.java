package perks.arrest;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import utils.TargetHelper;

import java.util.List;
import java.util.stream.Collectors;

public class CopUtil {

    public List<Player> getCops() {
        return Bukkit.getOnlinePlayers().stream().filter(this::isCop).collect(Collectors.toList());
    }

    public boolean isCop(Player player) {
        return player.hasPermission(new Permission("CR.Cop", PermissionDefault.FALSE));
    }

    public boolean isCopNear(Player player) {
        double radiusSquared = 10 * 10;

        List<Entity> entities = player.getNearbyEntities(10, 10, 10);
        for (Entity entity : entities) {

            if (entity.getLocation().distanceSquared(player.getLocation()) > radiusSquared) continue;

            if (entity instanceof Player) {
                Player p = (Player) entity;

                if (isCop(p) && !TargetHelper.isObstructed(p.getLocation(), player.getLocation())) {
                    return true;
                }
            }

        }


        return false;
    }

    public void setWanted(Player player) {
        Main.instance.perkManager.timer.put(player.getUniqueId(), new TimerObject(System.currentTimeMillis(), 120));
        //GlowAPI.setGlowing(player, GlowAPI.Color.RED, getCops());

        player.sendMessage(ChatColor.RED + "You are now wanted by the police!");
    }

    public boolean isWanted(Player player) {
        return Main.instance.perkManager.timer.containsKey(player.getUniqueId());
    }

    public void addCrime(Player player, PerkManager.Crime crime) {
        if (isWanted(player)) {
            TimerObject timerObject = Main.instance.perkManager.timer.get(player.getUniqueId());
            timerObject.seconds = timerObject.seconds + 30;
            timerObject.addCrime(crime);

            Main.instance.perkManager.timer.put(player.getUniqueId(), timerObject);
        }
    }

    public void logCrime(Player killer, PerkManager.Crime crime) {
        if (isCopNear(killer)) {
            if (isWanted(killer)) {
                addCrime(killer, crime);
            } else {
                setWanted(killer);
                addCrime(killer, crime);
            }
        }
    }
}
