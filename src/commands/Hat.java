package commands;

import com.zeshanaslam.crimering.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Hat implements CommandExecutor {

    private final Main plugin;

    public Hat(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("crhat")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (player.getInventory().getItemInMainHand() != null) {
                    player.getInventory().setHelmet(player.getInventory().getItemInMainHand());
                    player.getInventory().setItemInMainHand(null);
                }
            }
        }
        return false;
    }
}
