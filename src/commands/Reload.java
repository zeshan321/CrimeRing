package commands;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import script.ListenerObject;
import utils.ItemUtils;

import java.util.Iterator;
import java.util.UUID;

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

            // Reload script listeners
            Table<UUID, String, ListenerObject> temp = HashBasedTable.create();

            Iterator<Table.Cell<UUID, String, ListenerObject>> cellIterator = plugin.listeners.cellSet().iterator();
            while (cellIterator.hasNext()) {
                Table.Cell<UUID, String, ListenerObject> cell = cellIterator.next();

                System.out.println(cell.getColumnKey());
                cellIterator.remove();

                ListenerObject listenerObject = new ListenerObject(cell.getValue().scriptID, plugin.scriptsManager.getObject(cell.getValue().scriptID).script.getEngine(), cell.getValue().method);
                temp.put(cell.getRowKey(), cell.getColumnKey(), listenerObject);
            }

            plugin.listeners.putAll(temp);
            temp.clear();

            // Reload stackable items
            new ItemUtils().loadStackableItems();

            commandSender.sendMessage(ChatColor.RED + "CrimeRing has been reloaded!");
        }

        return false;
    }
}
