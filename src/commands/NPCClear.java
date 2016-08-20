package commands;

import com.zeshanaslam.crimering.Main;
import entity.EntityObject;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class NPCClear implements CommandExecutor {

    private final Main plugin;

    public NPCClear(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("npcclear")) {
            if (sender.isOp()) {
                for (EntityObject entityObject : plugin.entityManager.entityObjectList) {
                    entityObject.entity.remove();
                }

                plugin.entityManager.entityObjectList.clear();
                plugin.entityManager.entityUUID.clear();

                sender.sendMessage(ChatColor.RED + "Cleared all NPCs!");
            }
        }
        return false;
    }
}
