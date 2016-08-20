package commands;

import com.zeshanaslam.crimering.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Test implements CommandExecutor {

    private final Main plugin;

    public Test(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("test")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

            }
        }
        return false;
    }
}
