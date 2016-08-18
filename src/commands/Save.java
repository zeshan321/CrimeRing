package commands;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Save implements CommandExecutor {

    private final Main plugin;

    public Save(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("crsave")) {
            if (sender.isOp()) {
                System.out.println("[CR] Save worlds and player data...");
                Bukkit.savePlayers();

                for (String worlds: plugin.getConfig().getStringList("Save-worlds")) {
                    Bukkit.getWorld(worlds).save();
                }

                System.out.println("[CR] Save complete!");
            }
        }
        return false;
    }
}
