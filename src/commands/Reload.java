package commands;

import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Reload implements CommandExecutor {

    private final Main plugin;

    public Reload(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] strings) {
        if (commandLabel.equalsIgnoreCase("CRReload") && commandSender.isOp()) {
            Main.instance.reloadConfig();
            Main.instance.scriptsManager.load();

            commandSender.sendMessage(ChatColor.RED + "CrimeRing has been reloaded!");
        }

        return false;
    }
}
