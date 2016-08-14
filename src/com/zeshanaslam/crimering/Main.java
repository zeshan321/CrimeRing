package com.zeshanaslam.crimering;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import commands.Edit;
import commands.Item;
import commands.Reload;
import events.BasicEvents;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.NBTTagList;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import raids.PartyCommands;
import raids.RaidListener;
import raids.RaidManager;
import raids.RaidSetup;
import script.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Main extends JavaPlugin {

    public static Main instance;

    public ScriptsManager scriptsManager;
    public RaidManager raidManager;
    public WorldGuardPlugin worldGuardPlugin;
    public ArrayList<String> flag = new ArrayList<>();
    public HashMap<String, Integer> values = new HashMap<>();

    public static net.minecraft.server.v1_10_R1.ItemStack removeAttributes(net.minecraft.server.v1_10_R1.ItemStack i) {
        if (i == null) {
            return i;
        }
        if (net.minecraft.server.v1_10_R1.Item.getId(i.getItem()) == 386) {
            return i;
        }
        net.minecraft.server.v1_10_R1.ItemStack item = i.cloneItemStack();
        NBTTagCompound tag;
        if (!item.hasTag()) {
            tag = new NBTTagCompound();
            item.setTag(tag);
        } else {
            tag = item.getTag();
        }
        NBTTagList am = new NBTTagList();
        tag.set("AttributeModifiers", am);
        item.setTag(tag);
        return item;
    }

    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        // Load script manager
        scriptsManager = new ScriptsManager();
        scriptsManager.load();

        // Load raid manager
        raidManager = new RaidManager();

        // Load worldguard
        worldGuardPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");

        // User data dir
        File userDir = new File("plugins/CrimeRing/");
        if (!userDir.exists()) {
            userDir.mkdir();
        }

        // Scripts data dir
        File scriptDir = new File("plugins/CrimeRing/scripts/");
        if (!scriptDir.exists()) {
            scriptDir.mkdir();
        }

        // Scripts inv data dir
        File scriptInvDir = new File("plugins/CrimeRing/inv/");
        if (!scriptInvDir.exists()) {
            scriptInvDir.mkdir();
        }

        // Raids data dir
        File raidsDir = new File("plugins/CrimeRing/raids/");
        if (!raidsDir.exists()) {
            raidsDir.mkdir();
        }

        // Items data dir
        File itemsDir = new File("plugins/CrimeRing/items/");
        if (!itemsDir.exists()) {
            itemsDir.mkdir();
        }

        // events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BasicEvents(this), this);
        pm.registerEvents(new ActionBlocks(this), this);
        pm.registerEvents(new ActionCommands(this), this);
        pm.registerEvents(new ActionRegions(this), this);
        pm.registerEvents(new ActionNPC(this), this);
        pm.registerEvents(new ActionInv(this), this);
        pm.registerEvents(new PartyCommands(this), this);
        pm.registerEvents(new RaidListener(this), this);
        pm.registerEvents(new ActionDeath(this), this);

        // Commands
        getCommand("CRReload").setExecutor(new Reload(this));
        getCommand("raids").setExecutor(new RaidSetup(this));
        getCommand("action").setExecutor(new ActionCommands(this));
        getCommand("party").setExecutor(new PartyCommands(this));
        getCommand("CREdit").setExecutor(new Edit(this));
        getCommand("CRItem").setExecutor(new Item(this));

        // ProtocolLib remove attributes
        Set<PacketType> packets = new HashSet<>();
        packets.add(PacketType.Play.Server.SET_SLOT);
        packets.add(PacketType.Play.Server.WINDOW_ITEMS);
        packets.add(PacketType.Play.Server.CUSTOM_PAYLOAD);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, packets) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                PacketType type = packet.getType();
                if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                    try {
                        ItemStack[] read = packet.getItemArrayModifier().read(0);
                        for (int i = 0; i < read.length; i++) {
                            read[i] = removeAttributes(read[i]);
                        }
                        packet.getItemArrayModifier().write(0, read);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (type == PacketType.Play.Server.CUSTOM_PAYLOAD) {
                    if (!packet.getStrings().read(0).equalsIgnoreCase("MC|TrList")) {
                        return;
                    }

                    // Fix support
                } else {
                    try {
                        packet.getItemModifier().write(0, removeAttributes(packet.getItemModifier().read(0)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.CLIENT_COMMAND) {
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType().getLegacyId() == 22)
                    try {
                        if (event.getPlayer().hasMetadata("ironsights")) {
                            event.getPlayer().closeInventory();
                        }
                    } catch (FieldAccessException e) {
                        e.printStackTrace();
                    }
            }
        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
                    if (event.getPlayer().hasMetadata("ironsights") && !event.getPlayer().hasPotionEffect(PotionEffectType.SPEED)) {
                        event.getPlayer().updateInventory();
                    }
                }
            }
        });
    }

    public void onDisable() {
        saveConfig();
    }

    private ItemStack removeAttributes(ItemStack i) {
        if (i == null) {
            return i;
        }

        if (i.getType() == Material.BOOK_AND_QUILL) {
            return i;
        }

        ItemStack item = i.clone();
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

        return setUnbreakable(CraftItemStack.asCraftMirror(nmsStack));
    }

    private ItemStack setUnbreakable(ItemStack i) {
        if (!(i instanceof CraftItemStack)) {
            i = CraftItemStack.asCraftCopy(i);
        }
        NBTTagCompound tag = getTag(i);
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setInt("Unbreakable", 1);
        return setTag(i, tag);
    }

    private NBTTagCompound getTag(org.bukkit.inventory.ItemStack item) {
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
}
