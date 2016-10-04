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
                Player player = Bukkit.getPlayer(args[1]);

                if (player == null || !player.isOnline()) {
                    System.out.println("[CR] Unable to run script with offline player!");
                    return false;
                }

                if (Main.instance.scriptsManager.contains(script)) {
                    ScriptObject scriptObject = Main.instance.scriptsManager.getObject(script);

                    try {
                        ScriptEngine engine = Main.instance.scriptsManager.engine;
                        CompiledScript compiledScript = scriptObject.script;

                        // Objects
                        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                        bindings.put("player", player);

                        compiledScript.eval(bindings);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }

                    return true;
                }

                System.out.println("[CR] Unable to find script name!");
            }
        }
        return false;
    }
}
