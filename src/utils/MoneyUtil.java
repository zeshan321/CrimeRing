package utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MoneyUtil {

    private int bill1ID = 410;
    private int bill50ID = 409;
    private int bill2500ID = 370;

    public void giveBills(Player player, int amount) {
        while (true) {
            if (amount - 2500 >= 0) {
                amount = amount - 2500;
                player.getInventory().addItem(new ItemStack(bill2500ID, 1));
            } else {
                break;
            }
        }

        while (true) {
            if (amount - 50 >= 0) {
                amount = amount - 50;
                player.getInventory().addItem(new ItemStack(bill50ID, 1));
            } else {
                break;
            }
        }

        while (true) {
            if (amount - 1 >= 0) {
                amount = amount - 1;
                player.getInventory().addItem(new ItemStack(bill1ID, 1));
            } else {
                break;
            }
        }
    }

    public int getItemWorth(ItemStack item) {
        int worth = 0;
        String split = ChatColor.translateAlternateColorCodes('&', "Worth: &a");

        if (item != null && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();

            if (itemMeta.hasLore()) {
                for (String lines : itemMeta.getLore()) {
                    if (lines.contains("Worth")) {
                        worth = Integer.valueOf(lines.split(split)[1].replace("$", ""));
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