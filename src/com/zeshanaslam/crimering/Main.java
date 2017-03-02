package com.zeshanaslam.crimering;

import ambientsounds.AmbientManager;
import bank.BankListener;
import bounty.BountyCreate;
import bounty.BountyManager;
import brewing.BrewListener;
import brewing.BrewingManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.earth2me.essentials.Essentials;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import commands.*;
import conversation.Conversation;
import conversation.ConversationEvents;
import customevents.PlayerEquipEvent;
import customevents.PlayerUnequipEvent;
import damage.DamageListener;
import damage.DamageManager;
import detection.EntityDetection;
import entity.EntityListener;
import entity.EntityManager;
import entity.EntityObject;
import events.*;
import fakeblocks.FakeblockCommand;
import fakeblocks.FakeblockListener;
import glow.EntityGlowHelper;
import glow.GlowListener;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import locks.LockCommand;
import locks.LockListener;
import locks.LockManager;
import locks.LocksCreate;
import merchants.SMerchantAPI;
import merchants.api.MerchantAPI;
import merchants.api.Merchants;
import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;
import packets.WrapperPlayClientWindowClick;
import payday.PaydayListener;
import payday.PaydayManager;
import perks.CooldownManager;
import perks.arrest.GlowPerk;
import perks.arrest.PerkManager;
import perks.thief.Scramble;
import perks.tracking.TrackingEvents;
import perks.tracking.TrackingManager;
import radio.RadioManager;
import raids.*;
import renamer.RenamerManager;
import resourcepack.ResourceCommand;
import resourcepack.ResourceListener;
import script.*;
import utils.ItemUtils;
import utils.ProtocolUtil;

import java.io.File;
import java.util.*;

public class Main extends JavaPlugin {

    public static Main instance;

    // Hooks
    public Economy economy;
    public ClanManager clanManager;
    public ZPermissionsService permissions;
    public Essentials essentials;

    // Managers
    public ScriptsManager scriptsManager;
    public RaidManager raidManager;
    public EntityManager entityManager;
    public RenamerManager renamerManager;
    public BrewingManager brewingManager;
    public WorldGuardPlugin worldGuardPlugin;
    public MerchantAPI merchantAPI;
    public BukkitAPIHelper mythicAPI;
    public PerkManager perkManager;
    public LockManager lockManager;
    public AmbientManager ambientManager;
    public ActionDefaults actionDefaults;
    public EntityDetection entityDetection;
    public RadioManager radioManager;
    public DamageManager damageManager;
    public CooldownManager cooldownManager;
    public PaydayManager paydayManager;
    public EntityGlowHelper entityGlowHelper;
    public TrackingManager trackingManager;
    public Conversation conversation;
    public BountyManager bountyManager;

    // Lists and maps
    public ArrayList<String> flag = new ArrayList<>();
    public ArrayList<UUID> resourcepack = new ArrayList<>();
    public HashMap<String, Integer> values = new HashMap<>();
    public HashMap<String, String> globalFlags = new HashMap<>();
    public HashMap<String, String> fakeBlocks = new HashMap<>();
    public HashMap<String, String> fakeBlocksLocation = new HashMap<>();
    public HashMap<Integer, UUID> playerTasks = new HashMap<>();
    public Table<UUID, String, ListenerObject> listeners = HashBasedTable.create();
    public HashMap<UUID, String> objective = new HashMap<>();

    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        // Essentials
        essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");

        // Register placeholders
        new Placeholders();

        // Hook into vault
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }

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

        // Player data dir
        File playerDir = new File("plugins/CrimeRing/player/");
        if (!playerDir.exists()) {
            playerDir.mkdir();
        }

        // Fakeblocks data dir
        File fakeDir = new File("plugins/CrimeRing/fakeblocks/");
        if (!fakeDir.exists()) {
            fakeDir.mkdir();
        }

        // Locks data dir
        File locksDir = new File("plugins/CrimeRing/locks/");
        if (!locksDir.exists()) {
            locksDir.mkdir();
        }

        // bounty data dir
        File bountyDir = new File("plugins/CrimeRing/bounties/");
        if (!bountyDir.exists()) {
            bountyDir.mkdir();
        }

        // Conversation handler
        conversation = new Conversation();

        // Bounty Manager
        bountyManager = new BountyManager();

        // Tracking Manager
        trackingManager = new TrackingManager();

        // Glow helper
        entityGlowHelper = new EntityGlowHelper();

        // Hook into ZPermissions
        permissions = Bukkit.getServicesManager().load(ZPermissionsService.class);

        // Payday manager
        paydayManager = new PaydayManager();

        // Damage manager
        damageManager = new DamageManager();
        damageManager.load();

        // Hook into SimpleClans
        clanManager = SimpleClans.getInstance().getClanManager();

        // General and limited methods for non scripting
        actionDefaults = new ActionDefaults(null, null);

        // Setup entity detection manager
        entityDetection = new EntityDetection();

        // Setup cooldown manager
        cooldownManager = new CooldownManager();

        // Setup ambient manager
        //ambientManager = new AmbientManager();

        // Setup lock manager
        lockManager = new LockManager();
        lockManager.start();

        // Setup perk manager
        perkManager = new PerkManager();

        // Hook into MythicMobs
        mythicAPI = new BukkitAPIHelper();

        // Setup merchant api
        MerchantAPI api = new SMerchantAPI();
        Merchants.set(api);
        merchantAPI = api;

        // Load script manager
        scriptsManager = new ScriptsManager();
        scriptsManager.load();

        // Load renamer manager
        renamerManager = new RenamerManager();
        renamerManager.load();
        //renamerManager.loadListeners();

        // Load brewing manager
        brewingManager = new BrewingManager();
        brewingManager.load();

        // Load raid manager
        raidManager = new RaidManager();

        // Load entity manager
        entityManager = new EntityManager();

        // Load stackable items
        new ItemUtils().loadStackableItems();

        // Load worldguard
        worldGuardPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");

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
        pm.registerEvents(new BodiesEvents(this), this);
        pm.registerEvents(new EntityListener(this), this);
        pm.registerEvents(new PlayerEvents(this), this);
        pm.registerEvents(new ResourceListener(this), this);
        pm.registerEvents(new OffHand(this), this);
        pm.registerEvents(new ScriptListener(this), this);
        pm.registerEvents(new FakeblockListener(this), this);
        pm.registerEvents(new ActionEquip(this), this);
        pm.registerEvents(new ActionInteract(this), this);
        pm.registerEvents(new ActionGrow(this), this);
        pm.registerEvents(new BrewListener(this), this);
        pm.registerEvents(new CitizensShoot(this), this);
        pm.registerEvents(new ArrowHitBlockListener(this), this);
        pm.registerEvents(new LocksCreate(this), this);
        pm.registerEvents(new LockListener(this), this);
        //pm.registerEvents(new AmbientListener(this), this);
        pm.registerEvents(new BankListener(this), this);
        pm.registerEvents(new ActionTrade(this), this);
        pm.registerEvents(new ActionLockpick(this), this);
        pm.registerEvents(new Generators(this), this);
        pm.registerEvents(new ActionArrested(this), this);
        pm.registerEvents(new DamageListener(this), this);
        pm.registerEvents(new PaydayListener(this), this);
        pm.registerEvents(new GlowListener(this), this);
        pm.registerEvents(new ActionShoot(this), this);
        pm.registerEvents(new ConversationEvents(this), this);

        // Register perks
        pm.registerEvents(new GlowPerk(this), this);
        pm.registerEvents(new Scramble(this), this);
        pm.registerEvents(new TrackingEvents(this), this);

        // Commands
        getCommand("CRReload").setExecutor(new Reload(this));
        getCommand("raidsetup").setExecutor(new RaidSetup(this));
        getCommand("action").setExecutor(new ActionCommands(this));
        getCommand("party").setExecutor(new PartyCommands(this));
        getCommand("CREdit").setExecutor(new Edit(this));
        getCommand("CRItem").setExecutor(new Item(this));
        getCommand("clearchat").setExecutor(new ClearChat(this));
        getCommand("raid").setExecutor(new RaidCommands(this));
        getCommand("crhat").setExecutor(new Hat(this));
        getCommand("crsave").setExecutor(new Save(this));
        getCommand("npcclear").setExecutor(new NPCClear(this));
        getCommand("crrpupdate").setExecutor(new ResourceCommand(this));
        getCommand("CRFakeBlocks").setExecutor(new FakeblockCommand(this));
        getCommand("CRRunscript").setExecutor(new RunScript(this));
        getCommand("CRLock").setExecutor(new LocksCreate(this));
        getCommand("lock").setExecutor(new LockCommand(this));

        ProtocolUtil protocolUtil = new ProtocolUtil();

        // ProtocolLib remove attributes
        Set<PacketType> packets = new HashSet<>();
        packets.add(PacketType.Play.Server.SET_SLOT);
        packets.add(PacketType.Play.Server.WINDOW_ITEMS);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, packets) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                PacketType type = packet.getType();

                if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                    try {
                        List<ItemStack> read = packet.getItemListModifier().read(0);

                        for (int i = 0; i < read.size(); i++) {
                            read.set(i, protocolUtil.setItemHideFlags(read.get(i)));
                        }

                        packet.getItemListModifier().write(0, read);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        packet.getItemModifier().write(0, protocolUtil.setItemHideFlags(packet.getItemModifier().read(0)));
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
        final int POTION_INDEX = ProtocolLibrary.getProtocolManager().getMinecraftVersion().compareTo(new MinecraftVersion("1.11.2")) <= 0 ? 8 : 7;
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

        // Disable off hand
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.WINDOW_CLICK) {
            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientWindowClick packet = new WrapperPlayClientWindowClick(event.getPacket());

                if (packet.getSlot() == 45) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        ItemStack itemStack = event.getPlayer().getInventory().getItemInOffHand();

                        if (itemStack.getType() == Material.DIAMOND_HOE && itemStack.getDurability() == 497) {
                            return;
                        }

                        event.getPlayer().getInventory().setItemInOffHand(null);
                        event.getPlayer().getInventory().addItem(itemStack);
                        event.getPlayer().updateInventory();
                    }, 5L);
                }
            }
        });

        // Stop removing fake blocks
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this,
                PacketType.Play.Client.USE_ITEM) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();

                int x = event.getPacket().getBlockPositionModifier().read(0).getX();
                int y = event.getPacket().getBlockPositionModifier().read(0).getY();
                int z = event.getPacket().getBlockPositionModifier().read(0).getZ();
                String world = player.getWorld().getName();

                if (fakeBlocksLocation.containsKey(x + "-" + y + "-" + z + "-" + world)) {
                    event.setCancelled(true);
                }
            }
        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this,
                PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();

                int x = event.getPacket().getBlockPositionModifier().read(0).getX();
                int y = event.getPacket().getBlockPositionModifier().read(0).getY();
                int z = event.getPacket().getBlockPositionModifier().read(0).getZ();
                String world = player.getWorld().getName();

                if (fakeBlocksLocation.containsKey(x + "-" + y + "-" + z + "-" + world)) {
                    String[] data = fakeBlocksLocation.get(x + "-" + y + "-" + z + "-" + world).split(" ");

                    player.sendBlockChange(new Location(player.getWorld(), x, y, z), Material.matchMaterial(data[0]), (byte) Integer.parseInt(data[1]));
                }
            }
        });

        // Equip listener
        List<UUID> add = new ArrayList<>();
        List<UUID> remove = new ArrayList<>();
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this,
                PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();

                int slotIndex = packet.getIntegers().read(1);
                if (slotIndex >= 5 && slotIndex < 9) {
                    ItemStack itemStack = packet.getItemModifier().read(0);
                    if (itemStack == null) {
                        if (remove.contains(player.getUniqueId())) {
                            return;
                        }

                        remove.add(player.getUniqueId());
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            remove.remove(player.getUniqueId());

                            plugin.getServer().getPluginManager().callEvent(new PlayerUnequipEvent(player, itemStack, slotIndex));
                        }, 0);
                    } else {
                        if (add.contains(player.getUniqueId())) {
                            return;
                        }

                        add.add(player.getUniqueId());
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            add.remove(player.getUniqueId());

                            plugin.getServer().getPluginManager().callEvent(new PlayerEquipEvent(player, itemStack, slotIndex));
                        }, 0);
                    }
                }
            }
        });

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            Iterator it = BodiesEvents.bodies.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();

                BodyObject bodyObject = (BodyObject) pair.getValue();

                long secondsLeft = ((bodyObject.time / 1000) + 600) - (System.currentTimeMillis() / 1000);
                if (secondsLeft <= 0) {
                    it.remove();
                    bodyObject.loc.getWorld().getBlockAt(bodyObject.loc).setType(Material.AIR);
                }
            }
        }, 20L * 60, 20L * 60);
    }

    public void onDisable() {
        // Clear lists in case of reload
        scriptsManager.clear();
        raidManager.clear();
        entityManager.clear();
        brewingManager.clear();
        renamerManager.clear();

        // Save config
        saveConfig();

        // Close all locked doors
        lockManager.removeUnlocked();

        // Clear dead bodies
        Iterator it = BodiesEvents.bodies.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            BodyObject bodyObject = (BodyObject) pair.getValue();
            bodyObject.loc.getWorld().getBlockAt(bodyObject.loc).setType(Material.AIR);

            it.remove();
        }

        // Clear NPCs
        it = entityManager.entityObjectList.iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            EntityObject entityObject = (EntityObject) pair.getValue();
            entityObject.entity.remove();

            it.remove();
        }
    }
}
