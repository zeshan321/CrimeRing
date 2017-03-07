package commands;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Admin implements CommandExecutor {

    private final Main plugin;

    public Admin(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("cradmin")) {
            if (sender.isOp()) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;

                    if (Main.instance.admins.contains(player.getUniqueId())) {
                        player.sendMessage(ChatColor.GRAY + "Admin bypass has been " + ChatColor.RED + "disabled" + ChatColor.GRAY + ".");
                        Main.instance.admins.remove(player.getUniqueId());
                    } else {
                        player.sendMessage(ChatColor.GRAY + "Admin bypass has been " + ChatColor.RED + "enabled" + ChatColor.GRAY + ".");
                        Main.instance.admins.add(player.getUniqueId());
                    }
                }
            }
        }
        return false;
    }
}
