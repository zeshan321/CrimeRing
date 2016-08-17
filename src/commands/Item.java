package commands;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Item implements CommandExecutor {

    private final Main plugin;

    public Item(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("CRItem") && commandSender.isOp()) {
            if (args.length == 0) {
                commandSender.sendMessage(ChatColor.GOLD + "/CRItem save <name>");
                commandSender.sendMessage(ChatColor.GOLD + "/CRItem get <name>");
                commandSender.sendMessage(ChatColor.GOLD + "/CRItem raid <name>");
                return false;
            }

            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(ChatColor.RED + "This command can only be runned in-game!");
            }

            Player player = (Player) commandSender;

            if (args[0].equalsIgnoreCase("save")) {
                if (args.length < 1) {
                    commandSender.sendMessage(ChatColor.GOLD + "/CRItem save <name>");
                    return false;
                }

                if (player.getInventory().getItemInMainHand() == null) {
                    player.sendMessage(ChatColor.RED + "Needs to be a item in your hand!");
                    return false;
                }

                FileHandler fileHandler = new FileHandler("plugins/CrimeRing/items/" + args[1] + ".yml");
                fileHandler.set("Item", player.getInventory().getItemInMainHand());
                fileHandler.save();

                player.sendMessage(ChatColor.GOLD + "Item saved!");
            }

            if (args[0].equalsIgnoreCase("get")) {
                if (args.length < 1) {
                    commandSender.sendMessage(ChatColor.GOLD + "/CRItem get <name>");
                    return false;
                }

                int amount = 1;

                if (args.length > 2) {
                    amount = Integer.parseInt(args[2]);
                }

                FileHandler fileHandler = new FileHandler("plugins/CrimeRing/items/" + args[1] + ".yml");

                ItemStack item = fileHandler.getItemStack("Item");
                item.setAmount(amount);

                player.getInventory().addItem(item);
                player.sendMessage(ChatColor.GOLD + "Added item saved!");
            }

            if (args[0].equalsIgnoreCase("raid")) {
                if (args.length < 1) {
                    commandSender.sendMessage(ChatColor.GOLD + "/CRItem get <name>");
                    return false;
                }

                int amount = 1;

                if (args.length > 2) {
                    amount = Integer.parseInt(args[2]);
                }

                FileHandler fileHandler = new FileHandler("plugins/CrimeRing/items/" + args[1] + ".yml");

                ItemStack item = fileHandler.getItemStack("Item");
                item.setAmount(amount);

                ItemMeta itemMeta = item.getItemMeta();
                List<String> lore = new ArrayList<>();

                if (itemMeta.hasLore()) {
                    lore = itemMeta.getLore();
                }

                lore.add(ChatColor.GOLD + "Raid Item");
                itemMeta.setLore(lore);

                item.setItemMeta(itemMeta);

                player.getInventory().addItem(item);
                player.sendMessage(ChatColor.GOLD + "Added item saved!");
            }
        }
        return false;
    }
}
