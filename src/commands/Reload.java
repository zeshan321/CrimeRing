package commands;

import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import utils.ItemUtils;

public class Reload implements CommandExecutor {

    private final Main plugin;

    public Reload(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] strings) {
        if (commandLabel.equalsIgnoreCase("CRReload") && commandSender.isOp()) {
            plugin.reloadConfig();

            // Reload managers
            plugin.scriptsManager.load();
            plugin.renamerManager.load();
            plugin.brewingManager.load();
            plugin.damageManager.load();
            //plugin.radioManager.load();

            // Reload stackable items
            new ItemUtils().loadStackableItems();

            commandSender.sendMessage(ChatColor.RED + "CrimeRing has been reloaded!");
        }

        return false;
    }
}
