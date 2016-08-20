package script;

import com.zeshanaslam.crimering.FileHandler;
import org.bukkit.ChatColor;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class ScriptsManager {

    public HashMap<String, ScriptObject> scriptData = new HashMap<>();
    public ScriptEngineManager factory;
    public ScriptEngine engine;


    public ScriptsManager() {
        this.factory = new ScriptEngineManager();
        this.engine = factory.getEngineByName("nashorn");
    }

    public void load() {
        scriptData.clear();

        FileHandler imports = new FileHandler("plugins/CrimeRing/scripts.yml");

        int loaded = 0;

        for (String data : imports.getKeys()) {
            String dir = imports.getString(data + ".dir");

            try {
                String script = ChatColor.translateAlternateColorCodes('&', String.join("\n", Files.readAllLines(Paths.get("plugins/CrimeRing/scripts/" + File.separator + dir))).replace("\n", "").replace("\t", ""));

                ScriptEngine engine = this.engine;
                Compilable compilableEngine = (Compilable) engine;
                CompiledScript compiledScript = compilableEngine.compile(script);

                scriptData.put(data, new ScriptObject(data, dir, compiledScript));
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
            }

            loaded = loaded + 1;
        }

        System.out.println("[CR] Loaded " + loaded + " scripts.");
    }

    public boolean contains(String key) {
        return scriptData.containsKey(key);
    }

    public ScriptObject getObject(String key) {
        return scriptData.get(key);
    }
}
