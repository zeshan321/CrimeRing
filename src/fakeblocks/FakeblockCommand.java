package fakeblocks;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FakeblockCommand implements CommandExecutor {

    private final Main plugin;

    public FakeblockCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("CRFakeBlocks")) {
            if (sender.isOp()) {

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.GOLD + "/CRFakeBlocks start <file>");
                    sender.sendMessage(ChatColor.GOLD + "/CRFakeBlocks stop <file>");
                    return false;
                }

                String type = args[0];
                String fileName = args[1];
                Player player = (Player) sender;

                FileHandler yaml = new FileHandler("plugins/CrimeRing/fakeblocks/" + fileName + ".yml");

                if (type.equalsIgnoreCase("Start")) {
                    yaml.set("Editing", true);
                    yaml.save();

                    plugin.fakeBlocks.put(player.getName(), fileName);

                    if (yaml.contains("List")) {
                        player.sendMessage(ChatColor.GOLD + "Loading blocks.");

                        for (String location : getFileContent(fileName)) {
                            String[] location1 = location.split(" ");
                            int x = Integer.parseInt(location1[0]);
                            int y = Integer.parseInt(location1[1]);
                            int z = Integer.parseInt(location1[2]);
                            String world = location1[3];
                            Material material = Material.matchMaterial(location1[4]);
                            Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                            Block block = Bukkit.getWorld(world).getBlockAt(loc);
                            block.setType(material);
                        }

                        player.sendMessage(ChatColor.GOLD + "Loading complete!");
                    }

                    player.sendMessage(ChatColor.GOLD + "Now editing " + fileName + "!");
                    return false;
                }
                if (type.equalsIgnoreCase("Stop")) {
                    yaml.set("Editing", false);
                    yaml.save();

                    plugin.fakeBlocks.remove(player.getName());

                    if (yaml.contains("List")) {
                        player.sendMessage(ChatColor.GOLD + "Removing blocks.");

                        for (String location : getFileContent(fileName)) {
                            String[] location1 = location.split(" ");
                            int x = Integer.parseInt(location1[0]);
                            int y = Integer.parseInt(location1[1]);
                            int z = Integer.parseInt(location1[2]);
                            String world = location1[3];
                            Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                            Block block = Bukkit.getWorld(world).getBlockAt(loc);
                            block.setType(Material.AIR);
                        }

                        player.sendMessage(ChatColor.GOLD + "Removing complete!");
                    }
                    player.sendMessage(ChatColor.GOLD + "Saved " + fileName + "!");
                }
            }
        }
        return false;
    }

    private List<String> getFileContent(String file) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/fakeblocks/" + file + ".yml");

        return fileHandler.getStringList("List");
    }
}