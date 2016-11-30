package script;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;

import javax.script.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ScriptsManager {

    public ScriptEngine engine;
    private Multimap<String, ScriptObject> scriptData = HashMultimap.create();

    public void load() {
        this.engine = new ScriptEngineManager().getEngineByName("nashorn");

        scriptData.clear();

        FileHandler imports = new FileHandler("plugins/CrimeRing/scripts.yml");

        int loaded = 0;

        for (String data : imports.getKeys()) {
            String dir = imports.getString(data + ".dir");

            try {
                FileReader reader = new FileReader("plugins/CrimeRing/scripts/" + File.separator + dir);
                BufferedReader textReader = new BufferedReader(reader);

                String line;
                String script = "";
                while ((line = textReader.readLine()) != null) {

                    if (line.contains("//")) {
                        continue;
                    }

                    script += line;
                }

                script = ChatColor.translateAlternateColorCodes('&', String.join("\n", script).replace("\n", "").replace("\t", ""));

                scriptData.put(data, new ScriptObject(data, dir, script));
            } catch (IOException e) {
                System.out.println("[CR] Error for file: " + dir);
                e.printStackTrace();
            }

            loaded = loaded + 1;
        }

        System.out.println("[CR] Loaded " + loaded + " scripts.");

        Main.instance.listeners.clear();
        System.out.println("[CR] Cleared listeners.");
    }

    public void clear() {
        scriptData.clear();
    }

    public boolean contains(String key) {
        return scriptData.containsKey(key);
    }

    public ScriptObject getObject(String key) {
        return Iterables.get(scriptData.get(key), 0);
    }
}
