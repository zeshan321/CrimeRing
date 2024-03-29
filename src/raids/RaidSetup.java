package raids;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RaidSetup implements CommandExecutor {

    private final Main plugin;

    public RaidSetup(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("raidsetup") && sender.isOp()) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.GOLD + "/raidsetup create <name>");
                sender.sendMessage(ChatColor.GOLD + "/raidsetup delete <name>");
                sender.sendMessage(ChatColor.GOLD + "/raidsetup edit <name> <max | min | spawn | name | start> <value>");
                return false;
            }

            if (args[0].equalsIgnoreCase("create")) {
                new FileHandler("plugins/CrimeRing/raids/" + args[1] + ".yml");
                sender.sendMessage(ChatColor.GOLD + "Created raid: " + args[1] + ".");
            }

            if (args[0].equalsIgnoreCase("delete")) {
                new FileHandler("plugins/CrimeRing/raids/" + args[1] + ".yml").delete();
                sender.sendMessage(ChatColor.GOLD + "Deleted raid: " + args[1] + ".");
            }

            if (args[0].equalsIgnoreCase("edit")) {
                String name = args[1];
                String type = args[2];

                FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + name + ".yml");

                if (type.equalsIgnoreCase("min")) {
                    fileHandler.set("info.min", Integer.valueOf(args[3]));
                    fileHandler.save();

                    sender.sendMessage(ChatColor.GOLD + "Set min players at " + args[3] + " for " + name + ".");
                    return false;
                }

                if (type.equalsIgnoreCase("max")) {
                    fileHandler.set("info.max", Integer.valueOf(args[3]));
                    fileHandler.save();

                    sender.sendMessage(ChatColor.GOLD + "Set max players at " + args[3] + " for " + name + ".");
                    return false;
                }

                if (type.equalsIgnoreCase("name")) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 3; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    String raidName = sb.toString().trim();

                    fileHandler.set("info.name", raidName);
                    fileHandler.save();

                    sender.sendMessage(ChatColor.GOLD + "Set raid name " + raidName + " for " + name + ".");
                    return false;
                }

                if (type.equalsIgnoreCase("start")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        fileHandler.set("info.xs", player.getLocation().getX());
                        fileHandler.set("info.ys", player.getLocation().getY());
                        fileHandler.set("info.zs", player.getLocation().getZ());
                        fileHandler.set("info.pitchs", player.getLocation().getPitch());
                        fileHandler.set("info.yaws", player.getLocation().getYaw());
                        fileHandler.set("info.worlds", player.getLocation().getWorld().getName());
                        fileHandler.save();

                        sender.sendMessage(ChatColor.GOLD + "Set start for " + name + ".");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You need to be in-game to run this command!");
                    }
                    return false;
                }

                if (type.equalsIgnoreCase("spawn")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        fileHandler.set("info.x", player.getLocation().getX());
                        fileHandler.set("info.y", player.getLocation().getY());
                        fileHandler.set("info.z", player.getLocation().getZ());
                        fileHandler.set("info.pitch", player.getLocation().getPitch());
                        fileHandler.set("info.yaw", player.getLocation().getYaw());
                        fileHandler.set("info.world", player.getLocation().getWorld().getName());
                        fileHandler.save();

                        sender.sendMessage(ChatColor.GOLD + "Set spawn for " + name + ".");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You need to be in-game to run this command!");
                    }
                    return false;
                }
            }
        }

        return false;
    }
}
