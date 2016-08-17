package raids;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
                sender.sendMessage(ChatColor.GOLD + "/raid leave - This command will kick you and your party from the raid.");

                if (sender.isOp()) {
                    sender.sendMessage(ChatColor.GOLD + "/raid kick <name> - This command will kick the player and his party from a raid.");
                }
                return false;
            }

            if (args[0].equalsIgnoreCase("leave")) {
                Player player = (Player) sender;

                PartyAPI partyAPI = new PartyAPI();
                PartyObject partyObject = partyAPI.getParty(player);

                if (partyObject == null) {
                    if (Main.instance.raidManager.raids.containsKey(player)) {
                        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + Main.instance.raidManager.raids.get(player) + ".yml");

                        player.sendMessage(ChatColor.RED + "You have left the raid!");
                        player.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"), fileHandler.getInteger("info.yaws"), fileHandler.getInteger("info.pitchs")));

                        Main.instance.raidManager.cancelRaid(player);
                        return false;
                    }
                }

                if (partyAPI.isOwner(player)) {
                    if (Main.instance.raidManager.raids.containsKey(player)) {
                        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + Main.instance.raidManager.raids.get(player) + ".yml");

                        for (Player players : partyObject.getMembers()) {
                            players.sendMessage(ChatColor.RED + "You have been kicked from your raid because " + player.getName() + " left the raid!");
                            players.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"), fileHandler.getInteger("info.yaws"), fileHandler.getInteger("info.pitchs")));
                        }

                        Main.instance.raidManager.cancelRaid(player);
                    }

                    partyObject.sendMessage(ChatColor.RED + "You have been kicked from your raid because " + player.getName() + " left the raid!");

                    for (int a = 0; a < Main.instance.raidManager.invites.size(); a++) {
                        if ((Main.instance.raidManager.invites.get(a)).party == partyAPI.getParty(player)) {
                            Main.instance.raidManager.invites.remove(a);
                        }
                    }

                    Main.instance.raidManager.parties.remove(partyObject);
                    return false;
                }

                if (Main.instance.raidManager.raids.containsKey(partyObject.getOwner())) {
                    FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + Main.instance.raidManager.raids.get(player) + ".yml");

                    for (Player players : partyObject.getMembers()) {
                        players.sendMessage(ChatColor.RED + "You have been kicked from your raid because " + player.getName() + " left the raid!");
                        players.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"), fileHandler.getInteger("info.yaws"), fileHandler.getInteger("info.pitchs")));
                    }

                    Main.instance.raidManager.cancelRaid(player);
                    return false;
                }

                player.sendMessage(ChatColor.RED + "You are not in a raid!");
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

                PartyAPI partyAPI = new PartyAPI();
                PartyObject partyObject = partyAPI.getParty(player);

                if (partyObject == null) {
                    if (Main.instance.raidManager.raids.containsKey(player)) {
                        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + Main.instance.raidManager.raids.get(player) + ".yml");

                        player.sendMessage(ChatColor.RED + "You have been kicked from the raid by an Admin.");
                        player.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"), fileHandler.getInteger("info.yaws"), fileHandler.getInteger("info.pitchs")));

                        Main.instance.raidManager.cancelRaid(player);
                        sender.sendMessage(ChatColor.GOLD + "Kicked " + player.getName() + " from raid.");
                        return false;
                    }
                }

                if (partyAPI.isOwner(player)) {
                    if (Main.instance.raidManager.raids.containsKey(player)) {
                        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + Main.instance.raidManager.raids.get(player) + ".yml");

                        for (Player players : partyObject.getMembers()) {
                            players.sendMessage(ChatColor.RED + "You have been kicked from the raid by an Admin.");
                            players.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"), fileHandler.getInteger("info.yaws"), fileHandler.getInteger("info.pitchs")));
                        }

                        Main.instance.raidManager.cancelRaid(player);
                    }

                    partyObject.sendMessage(ChatColor.RED + "You have been kicked from the raid by an Admin.");

                    for (int a = 0; a < Main.instance.raidManager.invites.size(); a++) {
                        if ((Main.instance.raidManager.invites.get(a)).party == partyAPI.getParty(player)) {
                            Main.instance.raidManager.invites.remove(a);
                        }
                    }

                    Main.instance.raidManager.parties.remove(partyObject);
                    sender.sendMessage(ChatColor.GOLD + "Kicked " + player.getName() + " from raid.");
                    return false;
                }

                if (Main.instance.raidManager.raids.containsKey(partyObject.getOwner())) {
                    FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + Main.instance.raidManager.raids.get(player) + ".yml");

                    for (Player players : partyObject.getMembers()) {
                        players.sendMessage(ChatColor.RED + "You have been kicked from the raid by an Admin.");
                        players.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"), fileHandler.getInteger("info.yaws"), fileHandler.getInteger("info.pitchs")));
                    }

                    Main.instance.raidManager.cancelRaid(player);
                    sender.sendMessage(ChatColor.GOLD + "Kicked " + player.getName() + " from raid.");
                    return false;
                }

                sender.sendMessage(ChatColor.RED + player.getName() + " is currently not in a raid.");
            }
        }

        return false;
    }
}
