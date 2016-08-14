package commands;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

public class Edit implements CommandExecutor {

    private final Main plugin;

    public Edit(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("CREdit")) {
            if (sender.isOp()) {
                String world = args[0];
                int x = Integer.parseInt(args[1]);
                int y = Integer.parseInt(args[2]);
                int z = Integer.parseInt(args[3]);
                int x1 = Integer.parseInt(args[4]);
                int y1 = Integer.parseInt(args[5]);
                int z1 = Integer.parseInt(args[6]);
                Material material = Material.getMaterial(args[7]);

                Location point1 = new Location(Bukkit.getWorld(world), x, y, z);
                Location point2 = new Location(Bukkit.getWorld(world), x1, y1, z1);

                Vector max = Vector.getMaximum(point1.toVector(), point2.toVector());
                Vector min = Vector.getMinimum(point1.toVector(), point2.toVector());

                int data = 0;
                if (args.length > 8) {
                    data = Integer.parseInt(args[8]);
                }
                for (int i = min.getBlockX(); i <= max.getBlockX(); i++) {
                    for (int j = min.getBlockY(); j <= max.getBlockY(); j++) {
                        for (int k = min.getBlockZ(); k <= max.getBlockZ(); k++) {
                            Block block = Bukkit.getServer().getWorld(world).getBlockAt(i, j, k);
                            block.setType(material);
                            block.setData((byte) data);
                        }
                    }
                }
            }
        }
        return false;
    }
}
