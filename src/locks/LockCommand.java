package locks;

import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;
import java.util.List;

public class LockCommand implements CommandExecutor {

    private final Main plugin;

    public LockCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("lock")) {
            if (args.length < 3) {
                lockHelp(sender);
                return false;
            }

            Player player = (Player) sender;

            if (player.getInventory().getItemInMainHand() == null) {
                player.sendMessage(ChatColor.RED + "You need to be holding a lock in your hand!");
                return false;
            }

            ItemStack itemStack = player.getInventory().getItemInMainHand();

            if (!Main.instance.lockManager.chests.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-lock")) {
                player.sendMessage(ChatColor.RED + "You need to be holding a lock in your hand!");
                return false;
            }

            int response = isOwner(player, itemStack);

            if (response == 0) {
                player.sendMessage(ChatColor.RED + "This lock needs to be activated! Right click the lock to active it.");
                return false;
            }

            if (response == 2) {
                player.sendMessage(ChatColor.RED + "Only the owner of the lock can add or remove players/gangs.");
                return false;
            }

            if (args[0].equalsIgnoreCase("add")) {
                if (args[1].equalsIgnoreCase("player") || args[1].equalsIgnoreCase("gang")) {
                    player.getInventory().setItemInMainHand(add(args[1], args[2], itemStack));

                    player.sendMessage(ChatColor.GOLD + "Added " + args[1].toLowerCase() + " " + args[2] + " to lock!");
                } else {
                    lockHelp(sender);
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args[1].equalsIgnoreCase("player") || args[1].equalsIgnoreCase("gang")) {
                    player.getInventory().setItemInMainHand(remove(args[1], args[2], itemStack));

                    player.sendMessage(ChatColor.GOLD + "Removed " + args[1].toLowerCase() + " " + args[2] + " from lock!");
                } else {
                    lockHelp(sender);
                }
            } else {
                lockHelp(sender);
            }
        }
        return false;
    }

    private void lockHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "/lock add <player | gang> <name>");
        sender.sendMessage(ChatColor.GOLD + "/lock remove <player | gang> <name>");
    }

    private int isOwner(Player player, ItemStack itemStack) {
        if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore() || ChatColor.stripColor(itemStack.getItemMeta().getLore().get(0)).contains("unactivated")) {
            return 0;
        }

        for (String lore : itemStack.getItemMeta().getLore()) {
            lore = ChatColor.stripColor(lore);

            if (lore.startsWith("Owner: ")) {
                String owner = lore.replace("Owner: ", "");

                if (owner.equals(player.getName())) {
                    return 1;
                }
            }
        }

        return 2;
    }

    private ItemStack add(String type, String user, ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> lore = itemMeta.getLore();

        if (type.equalsIgnoreCase("gang")) {
            lore.add(ChatColor.GREEN + "Gang: " + user);
        } else {
            lore.add(ChatColor.GREEN + user);
        }

        itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    private ItemStack remove(String type, String user, ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> lore = itemMeta.getLore();

        if (type.equalsIgnoreCase("gang")) {
            lore.add(ChatColor.GREEN + "Gang: " + user);
        } else {
            lore.add(ChatColor.GREEN + user);
        }

        Iterator<String> iterator = lore.iterator();
        while(iterator.hasNext()) {
            String remove = ChatColor.stripColor(iterator.next());

            if (type.equals("gang")) {
                if (remove.startsWith("Gang: ")) {
                    String gang = remove.replace("Gang: ", "");

                    if (gang.equals(user)) {
                        iterator.remove();
                    }
                }
            } else {
                if (remove.equals(user)) {
                    iterator.remove();
                }
            }
        }

        itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}
