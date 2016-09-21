package raids;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RaidCommands implements CommandExecutor {

    private final Main plugin;

    public RaidCommands(Main plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("raid")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.GOLD + "/raid leave - This command will kick you from the raid.");

                if (sender.isOp()) {
                    sender.sendMessage(ChatColor.GOLD + "/raid kick <name> - This command will kick the player and his party from a raid.");
                }
                return false;
            }

            if (args[0].equalsIgnoreCase("leave")) {
                Player player = (Player) sender;

                RaidObject raidObject = plugin.raidManager.getRaid(player);

                if (raidObject == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a raid!");
                    return false;
                }

                raidObject.removeMember(player);
            }

            if (args[0].equalsIgnoreCase("kick")) {
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "[Raid] " + ChatColor.GOLD + "/raid kick <name>");
                    return false;
                }

                Player player = Bukkit.getPlayer(args[1]);

                if (!player.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Could not find " + player.getName());
                    return false;
                }

                RaidObject raidObject = plugin.raidManager.getRaid(player);

                if (raidObject == null) {
                    sender.sendMessage(ChatColor.RED + player.getName() + " is currently not in a raid.");
                    return false;
                }

                raidObject.removeMember(player);

                player.sendMessage(ChatColor.RED + "You have been kicked from the raid by an Admin!");
                sender.sendMessage(ChatColor.RED + "Kicked " + player.getName() + " from raid!");
            }
        }

        return false;
    }
}