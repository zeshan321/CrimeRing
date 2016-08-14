package com.zeshanaslam.crimering;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import commands.Edit;
import commands.Item;
import commands.Reload;
import events.BasicEvents;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import raids.PartyCommands;
import raids.RaidListener;
import raids.RaidManager;
import raids.RaidSetup;
import script.*;
import utils.ProtocolUtil;

import java.io.File;
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

        ProtocolUtil protocolUtil = new ProtocolUtil();

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
                            read[i] = protocolUtil.removeAttributes(read[i]);
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
                        packet.getItemModifier().write(0, protocolUtil.removeAttributes(packet.getItemModifier().read(0)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        // Don't allow opening inventory while scoped in.
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

        // Cancel scope when effect runs out
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

        // Hide potion particles
        final int POTION_INDEX = ProtocolLibrary.getProtocolManager().getMinecraftVersion().compareTo(new MinecraftVersion("1.10.2")) <= 0 ? 8 : 7;
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this,
                PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Entity entity = event.getPacket().getEntityModifier(event).read(0);

                if (event.getPlayer().equals(entity)) {
                    protocolUtil.modifyWatchable(event, POTION_INDEX, 0);
                }
            }
        });
    }

    public void onDisable() {
        saveConfig();
    }
}
