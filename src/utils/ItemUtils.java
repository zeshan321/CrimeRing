package utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class ItemUtils {

    public ItemStack createItem(Material material, String name, String description) {
        ItemStack returnStack = new ItemStack(material);
        ItemMeta meta = returnStack.getItemMeta();
        meta.setDisplayName(name);
        ArrayList<String> lore = new ArrayList<String>();
        if (description.contains("/n")) {
            String[] loreS = description.split("/n");
            for (String s : loreS) {
                lore.add(s);
            }
        }
        if (!(description.contains("/n"))) {
            lore.add(description);
        }
        meta.setLore(lore);

        returnStack.setItemMeta(meta);

        return returnStack;
    }

    public ItemStack createItem(Material material, String name, String description, int amount, int btyeData) {
        ItemStack returnStack = new ItemStack(material, amount, (short) btyeData);
        ItemMeta meta = returnStack.getItemMeta();
        meta.setDisplayName(name);
        ArrayList<String> lore = new ArrayList<String>();
        if (description.contains("/n")) {
            String[] loreS = description.split("/n");
            for (String s : loreS) {
                lore.add(s);
            }
        }
        if (!(description.contains("/n"))) {
            lore.add(description);
        }
        meta.setLore(lore);
        returnStack.setItemMeta(meta);
        return returnStack;
    }
}
