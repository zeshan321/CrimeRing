package commands;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearChat implements CommandExecutor {

    private final Main plugin;

    public ClearChat(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("/clearchat")) {
            if (sender.isOp()) {
                for (int i = 0; i < 100; i++) {
                    Bukkit.broadcastMessage("");
                }
            }
        }
        return false;
    }
}
