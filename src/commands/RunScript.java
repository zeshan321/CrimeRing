package commands;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import script.ActionDefaults;
import script.ScriptObject;

import javax.script.*;

public class RunScript implements CommandExecutor {

    private final Main plugin;

    public RunScript(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("CRRunscript")) {
            if (sender.isOp()) {
                if (args.length < 2) {
                    sender.sendMessage("/CRRunscript <script name> <target player>");
                    return false;
                }

                String script = args[0];

                if (!args[1].equals("none")) {
                    Player player = Bukkit.getPlayer(args[1]);

                    if (player == null || !player.isOnline()) {
                        System.out.println("[CR] Unable to run script with offline player!");
                        return false;
                    }

                    if (Main.instance.scriptsManager.contains(script)) {
                        for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(script)) {

                            try {
                                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                                // Objects
                                Bindings bindings = engine.createBindings();
                                bindings.put("player", player);
                                bindings.put("x", player.getLocation().getBlockX());
                                bindings.put("y", player.getLocation().getBlockY());
                                bindings.put("z", player.getLocation().getBlockZ());
                                bindings.put("world", player.getLocation().getWorld().getName());
                                bindings.put("CR", new ActionDefaults(script, engine));

                                ScriptContext scriptContext = engine.getContext();
                                scriptContext.setBindings(bindings, scriptContext.ENGINE_SCOPE);

                                engine.eval(scriptObject.scriptData, scriptContext);
                            } catch (ScriptException e) {
                                e.printStackTrace();
                            }
                        }

                        return true;
                    }
                } else {
                    if (Main.instance.scriptsManager.contains(script)) {
                        for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(script)) {

                            try {
                                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                                // Objects
                                Bindings bindings = engine.createBindings();
                                bindings.put("CR", new ActionDefaults(script, engine));

                                ScriptContext scriptContext = engine.getContext();
                                scriptContext.setBindings(bindings, scriptContext.ENGINE_SCOPE);

                                engine.eval(scriptObject.scriptData, scriptContext);
                            } catch (ScriptException e) {
                                e.printStackTrace();
                            }
                        }

                        return true;
                    }
                }

                System.out.println("[CR] Unable to find script name!");
            }
        }
        return false;
    }
}
