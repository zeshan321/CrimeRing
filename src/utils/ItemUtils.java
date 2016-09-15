package utils;

import com.zeshanaslam.crimering.Main;
import haveric.stackableItems.config.Config;
import haveric.stackableItems.util.SIItems;
import net.minecraft.server.v1_10_R1.EntityLiving;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.NBTTagList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ItemUtils {

    private static NBTTagCompound getTag(org.bukkit.inventory.ItemStack item) {
        if ((item instanceof CraftItemStack)) {
            try {
                Field field = CraftItemStack.class.getDeclaredField("handle");
                field.setAccessible(true);
                return ((net.minecraft.server.v1_10_R1.ItemStack) field.get(item)).getTag();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public LivingEntity addCustomNBT(LivingEntity entity, String key, Object value) {
        if (entity == null) return null;
        Entity bukkitEntity = entity;
        net.minecraft.server.v1_10_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        // Writes the entity's NBT data to tag
        nmsEntity.c(tag);

        // Add custom NBT
        if (value instanceof Integer) {
            tag.setInt(key, (int) value);
        }

        if (value instanceof String) {
            tag.setString(key, (String) value);
        }

        // Write tag back
        ((EntityLiving) nmsEntity).a(tag);
        return entity;
    }

    private org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack item, NBTTagCompound tag) {
        CraftItemStack craftItem = null;
        if ((item instanceof CraftItemStack)) {
            craftItem = (CraftItemStack) item;
        } else {
            craftItem = CraftItemStack.asCraftCopy(item);
        }
        net.minecraft.server.v1_10_R1.ItemStack nmsItem = null;
        try {
            Field field = CraftItemStack.class.getDeclaredField("handle");
            field.setAccessible(true);
            nmsItem = (net.minecraft.server.v1_10_R1.ItemStack) field.get(item);
        } catch (Exception e) {
        }
        if (nmsItem == null) {
            nmsItem = CraftItemStack.asNMSCopy(craftItem);
        }
        nmsItem.setTag(tag);
        try {
            Field field = CraftItemStack.class.getDeclaredField("handle");
            field.setAccessible(true);
            field.set(craftItem, nmsItem);
        } catch (Exception e) {
        }
        return craftItem;
    }

    public ItemStack createItem(Material material, String name, String description) {
        ItemStack returnStack = new ItemStack(material);
        ItemMeta meta = returnStack.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
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
        List<String> lore = new ArrayList<>();
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

    public ItemStack removeAttributes(ItemStack item) {
        net.minecraft.server.v1_10_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (!nmsStack.hasTag()) {
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        } else {
            tag = nmsStack.getTag();
        }

        NBTTagList am = new NBTTagList();
        tag.set("AttributeModifiers", am);
        nmsStack.setTag(tag);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public void clearRaidItems(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta itemMeta = item.getItemMeta();

                if (itemMeta.hasLore()) {
                    if (itemMeta.getLore().contains(ChatColor.GOLD + "Raid Item")) {
                        player.getInventory().remove(item);
                    }
                }
            }
        }
    }

    public void loadStackableItems() {
        String cfgMin = "MIN";
        String cfgMax = "MAX";
        int ITEM_DEFAULT = -1;

        new File("plugins/StackableItems/items.yml").delete();

        File itemsFile = new File("plugins/StackableItems/items.yml");
        FileConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        itemsConfig.addDefault("allWorlds.default." + cfgMin, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.default." + cfgMax, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.player.testPlayer." + cfgMin, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.player.testPlayer." + cfgMax, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.group.testGroup." + cfgMin, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.group.testGroup." + cfgMax, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.inventory.chest." + cfgMin, ITEM_DEFAULT);
        itemsConfig.addDefault("allWorlds.inventory.chest." + cfgMax, ITEM_DEFAULT);

        itemsConfig.addDefault("testWorld.default." + cfgMin, ITEM_DEFAULT);
        itemsConfig.addDefault("testWorld.default." + cfgMax, ITEM_DEFAULT);
        itemsConfig.options().copyDefaults(true);
        Config.saveConfig(itemsConfig, itemsFile);

        SIItems.reload();
        for (String item : Main.instance.getConfig().getStringList("Stackable-items")) {
            int amount = Integer.valueOf(item.split(" ")[1]);
            int ID = Integer.valueOf(item.split(":")[0]);
            short dura = Short.valueOf(item.split(":")[1].replace(" " + amount, ""));

            Material material = Material.getMaterial(ID);
            SIItems.setMax("allWorlds", "default", "", material, dura, amount);
        }
    }
}
