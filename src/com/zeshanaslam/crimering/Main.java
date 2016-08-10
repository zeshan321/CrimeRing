package com.zeshanaslam.crimering;

import Events.BasicEvents;
import commands.Reload;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import raids.RaidManager;
import raids.RaidSetup;
import script.ActionBlocks;
import script.ActionCommands;
import script.ScriptsManager;

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

        // Raids data dir
        File raidstDir = new File("plugins/CrimeRing/raids/");
        if (!raidstDir.exists()) {
            raidstDir.mkdir();
        }

        // Events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BasicEvents(this), this);
        pm.registerEvents(new ActionBlocks(this), this);
        pm.registerEvents(new ActionCommands(this), this);

        // Commands
        getCommand("CRReload").setExecutor(new Reload(this));
        getCommand("raids").setExecutor(new RaidSetup(this));
        getCommand("action").setExecutor(new ActionCommands(this));
    }

    public void onDisable() {
        saveConfig();
    }
}
