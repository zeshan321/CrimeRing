package com.zeshanaslam.crimering;

import commands.Reload;
import events.BasicEvents;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import raids.PartyCommands;
import raids.RaidManager;
import raids.RaidSetup;
import script.*;

import java.io.File;

public class Main extends JavaPlugin {

    public static Main instance;

    public ScriptsManager scriptsManager;
    public RaidManager raidManager;

    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        // Load script manager
        scriptsManager = new ScriptsManager();
        scriptsManager.load();

        // Load raid manager
        raidManager = new RaidManager();

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
        File scriptInvDir = new File("plugins/CrimeRing/scripts/inv/");
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

        // Commands
        getCommand("CRReload").setExecutor(new Reload(this));
        getCommand("raids").setExecutor(new RaidSetup(this));
        getCommand("action").setExecutor(new ActionCommands(this));
        getCommand("party").setExecutor(new PartyCommands(this));
    }

    public void onDisable() {
        saveConfig();
    }
}
