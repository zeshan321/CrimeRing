package payday;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import perks.PerksUtil;

import java.util.HashMap;
import java.util.UUID;

public class PaydayManager {

    public HashMap<UUID, Long> players = new HashMap<>();

    private final int time = 900;
    private PerksUtil perksUtil;

    public PaydayManager() {
        perksUtil = new PerksUtil();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, () -> {
            for (Player playersOnline: Bukkit.getOnlinePlayers()) {
                if (players.containsKey(playersOnline.getUniqueId())) {
                    Player player = Bukkit.getPlayer(playersOnline.getUniqueId());

                    if (!player.isOnline() || Main.instance.essentials.getUser(player).isAfk()) {
                        players.remove(player.getUniqueId());
                        continue;
                    }

                    long secondsLeft = ((this.players.get(player.getUniqueId()) / 1000) + time) - (System.currentTimeMillis() / 1000);

                    if (secondsLeft <= 0) {
                        int amount = 300;

                        if (perksUtil.hasPermission(player, "CR.cop.payday.3")) {
                            amount = 1200;
                        } else if (perksUtil.hasPermission(player, "CR.cop.payday.2")) {
                            amount = 900;
                        } else if (perksUtil.hasPermission(player, "CR.cop.payday.1")) {
                            amount = 600;
                        }

                        players.put(player.getUniqueId(), System.currentTimeMillis());
                        player.sendMessage(ChatColor.GRAY + "You received " + ChatColor.RED + "$" + amount + ChatColor.GRAY + " for being on duty.");
                        Main.instance.actionDefaults.depositMoney(player, amount);
                    }
                }
            }
        }, 0, 20L);
    }
}
