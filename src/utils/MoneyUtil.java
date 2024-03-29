package utils;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import renamer.RenamerObject;

public class MoneyUtil {

    private int bill1ID = 410;
    private int bill50ID = 409;
    private int bill2500ID = 370;

    public boolean isMoney(ItemStack itemStack) {
        return itemStack.getTypeId() == 410 || itemStack.getTypeId() == 409 || itemStack.getTypeId() == 370;
    }

    public void giveBills(Player player, int amount) {
        while (true) {
            if (amount - 2500 >= 0) {
                amount = amount - 2500;
                player.getInventory().addItem(Main.instance.actionDefaults.createItemStackWithRenamer(bill2500ID, 1, 0));

            } else {
                break;
            }
        }

        while (true) {
            if (amount - 50 >= 0) {
                amount = amount - 50;
                player.getInventory().addItem(Main.instance.actionDefaults.createItemStackWithRenamer(bill50ID, 1, 0));

            } else {
                break;
            }
        }

        while (true) {
            if (amount - 1 >= 0) {
                amount = amount - 1;
                player.getInventory().addItem(Main.instance.actionDefaults.createItemStackWithRenamer(bill1ID, 1, 0));
            } else {
                break;
            }
        }
    }

    public ItemStack[] getBillsStack(int amount) {
        Inventory inventory = Bukkit.createInventory(null, 54, "");

        while (true) {
            if (amount - 2500 >= 0) {
                amount = amount - 2500;
                inventory.addItem(Main.instance.actionDefaults.createItemStackWithRenamer(bill2500ID, 1, 0));

            } else {
                break;
            }
        }

        while (true) {
            if (amount - 50 >= 0) {
                amount = amount - 50;
                inventory.addItem(Main.instance.actionDefaults.createItemStackWithRenamer(bill50ID, 1, 0));

            } else {
                break;
            }
        }

        while (true) {
            if (amount - 1 >= 0) {
                amount = amount - 1;
                inventory.addItem(Main.instance.actionDefaults.createItemStackWithRenamer(bill1ID, 1, 0));
            } else {
                break;
            }
        }

        return inventory.getContents();
    }

    public int getItemWorth(ItemStack item) {
        int worth = 0;

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String lines : item.getItemMeta().getLore()) {
                lines = ChatColor.stripColor(lines).toLowerCase();

                if (lines.startsWith("worth")) {
                    worth = Integer.valueOf(lines.replace("worth: $", ""));
                    break;
                }
            }
        } else {
            if (Main.instance.renamerManager.items.containsKey(item.getTypeId() + ":" + item.getDurability())) {
                RenamerObject renamerObject = Main.instance.renamerManager.items.get(item.getTypeId() + ":" + item.getDurability());

                if (renamerObject.lore == null) return 0;

                for (String lines : renamerObject.lore) {
                    lines = ChatColor.stripColor(lines).toLowerCase();

                    if (lines.startsWith("worth")) {
                        worth = Integer.valueOf(lines.replace("worth: $", ""));
                        break;
                    }
                }
            }
        }

        return worth;
    }

    public int getItemStreetWorth(ItemStack item) {
        int worth = 0;

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String lines : item.getItemMeta().getLore()) {
                lines = ChatColor.stripColor(lines).toLowerCase();

                if (lines.startsWith("street worth")) {
                    worth = Integer.valueOf(lines.replace("street worth: $", ""));
                    break;
                }
            }
        } else {
            if (Main.instance.renamerManager.items.containsKey(item.getTypeId() + ":" + item.getDurability())) {
                RenamerObject renamerObject = Main.instance.renamerManager.items.get(item.getTypeId() + ":" + item.getDurability());

                if (renamerObject.lore == null) return 0;

                for (String lines : renamerObject.lore) {
                    lines = ChatColor.stripColor(lines).toLowerCase();

                    if (lines.startsWith("street worth")) {
                        worth = Integer.valueOf(lines.replace("street worth: $", ""));
                        break;
                    }
                }
            }
        }

        return worth;
    }

    public int getInvMoney(Player player) {
        int bill2500 = 0;
        int bill50 = 0;
        int bill1 = 0;

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) {
                continue;
            }

            if (itemStack.getTypeId() == bill2500ID) {
                bill2500 = bill2500 + itemStack.getAmount();
            }

            if (itemStack.getTypeId() == bill50ID) {
                bill50 = bill50 + itemStack.getAmount();
            }

            if (itemStack.getTypeId() == bill1ID) {
                bill1 = bill1 + itemStack.getAmount();
            }
        }

        return (bill2500 * 2500) + (bill50 * 50) + (bill1);
    }

    public int getInvMoneyRemove(Player player) {
        int bill2500 = 0;
        int bill50 = 0;
        int bill1 = 0;

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) {
                continue;
            }

            if (itemStack.getTypeId() == bill2500ID) {
                bill2500 = bill2500 + itemStack.getAmount();
                player.getInventory().remove(itemStack);
            }

            if (itemStack.getTypeId() == bill50ID) {
                bill50 = bill50 + itemStack.getAmount();
                player.getInventory().remove(itemStack);
            }

            if (itemStack.getTypeId() == bill1ID) {
                bill1 = bill1 + itemStack.getAmount();
                player.getInventory().remove(itemStack);
            }
        }

        return (bill2500 * 2500) + (bill50 * 50) + (bill1);
    }

    public void takeMoney(Player player, int amount) {
        int total = getInvMoneyRemove(player) - amount;

        giveBills(player, total);
    }
}
