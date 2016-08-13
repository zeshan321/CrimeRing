package com.zeshanaslam.crimering;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import commands.Edit;
import commands.Reload;
import events.BasicEvents;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import raids.PartyCommands;
import raids.RaidListener;
import raids.RaidManager;
import raids.RaidSetup;
import script.*;

import java.io.File;
import java.util.ArrayList;

public class Main extends JavaPlugin {

    public static Main instance;

    public ScriptsManager scriptsManager;
    public RaidManager raidManager;
    public WorldGuardPlugin worldGuardPlugin;
    public ArrayList<String> flag = new ArrayList<String>();

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

        // Commands
        getCommand("CRReload").setExecutor(new Reload(this));
        getCommand("raids").setExecutor(new RaidSetup(this));
        getCommand("action").setExecutor(new ActionCommands(this));
        getCommand("party").setExecutor(new PartyCommands(this));
        getCommand("CREdit").setExecutor(new Edit(this));
    }

    public void onDisable() {
        saveConfig();

        // Move to command
        /* Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        PartyAPI partyAPI = new PartyAPI();
        for (Player p : players) {
            if (partyAPI.getParty(p) == null) {
                if (raidManager.raids.containsKey(p)) {
                    FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + raidManager.raids.get(p) + ".yml");

                    String[] message = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Kick-end")).split("/n");
                    p.sendMessage(message);
                    p.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"), fileHandler.getInteger("info.yaws"), fileHandler.getInteger("info.pitchs")));

                    raidManager.cancelRaid(p);
                }
            } else {
                p.chat("/party leave");
            }
        }*/
    }
}
