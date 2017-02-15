package perks.thief;

import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class Scramble implements Listener {

    private final Main plugin;
    private final String permission = "CR.perk.scramble";
    private final int cooldownTime = 300;

    public Scramble(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getHand().equals(EquipmentSlot.OFF_HAND) || !player.isSneaking()) {
            return;
        }

        if (event.getRightClicked() instanceof Player && plugin.cooldownManager.perksUtil.hasPermission(player, permission)) {
            Player victim = (Player) event.getRightClicked();

            if (victim.getLocation().getDirection().dot(player.getLocation().getDirection()) > 0) {
                if (plugin.cooldownManager.isInCooldown(player, permission) == 0) {
                    plugin.cooldownManager.addCooldown(player, permission, cooldownTime);
                    player.sendMessage(ChatColor.GRAY + "You scrambled " + ChatColor.RED + victim.getName() + ChatColor.GRAY + "'s inventory!");

                    for (int i = 0; i < 9; i++) {
                        int firstSlot = ThreadLocalRandom.current().nextInt(0, 9);
                        int secondSlot = ThreadLocalRandom.current().nextInt(0, 9);

                        ItemStack firstItem = null;
                        ItemStack secondItem = null;

                        if (victim.getInventory().getItem(firstSlot) != null) {
                            firstItem = victim.getInventory().getItem(firstSlot);
                        }

                        if (victim.getInventory().getItem(secondSlot) != null) {
                            secondItem = victim.getInventory().getItem(secondSlot);
                        }

                        if (secondItem != null) {
                            victim.getInventory().setItem(firstSlot, secondItem);
                        }

                        if (firstItem != null) {
                            victim.getInventory().setItem(secondSlot, firstItem);
                        }
                    }
                } else {
                    long seconds = plugin.cooldownManager.isInCooldown(player, permission);
                    player.sendMessage(ChatColor.GRAY + "You can use scramble again in " + ChatColor.RED + seconds + ChatColor.GRAY + " seconds!");
                }
            }
        }
    }
}
