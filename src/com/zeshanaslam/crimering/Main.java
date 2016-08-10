package com.zeshanaslam.crimering;

import Events.BasicEvents;
import commands.Reload;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import script.ActionBlocks;
import script.ScriptsManager;

import java.io.File;

public class Main extends JavaPlugin {

    public static Main instance;

    public ScriptsManager scriptsManager;

    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        // Load script manager
        scriptsManager = new ScriptsManager();
        scriptsManager.load();

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

        // Events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BasicEvents(this), this);
        pm.registerEvents(new ActionBlocks(this), this);

        // Command
        getCommand("CRReload").setExecutor(new Reload(this));
    }

    public void onDisable() {
        saveConfig();
    }
}
