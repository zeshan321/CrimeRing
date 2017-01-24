package script;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

public class ScriptsManager {

    public ScriptEngine engine;
    private Multimap<String, ScriptObject> scriptData = ArrayListMultimap.create();

    public void load() {
        this.engine = new ScriptEngineManager().getEngineByName("nashorn");

        scriptData.clear();

        int loaded = 0;

        try {
            try (BufferedReader br = new BufferedReader(new FileReader("plugins/CrimeRing/scripts.yml"))) {
                for (String line; (line = br.readLine()) != null; ) {
                    if (line.startsWith("#")) continue;

                    String[] separate = line.split(" = ", 2);
                    String data = separate[0];
                    String dir = separate[1].replace(" ", "");
                    File file = new File("plugins/CrimeRing/scripts/" + File.separator + dir);

                    if (!file.exists()) {
                        System.out.println("[CR] Error! File not found: " + data + " = " + dir);
                        continue;
                    }

                    FileReader reader = new FileReader(file);
                    BufferedReader textReader = new BufferedReader(reader);

                    String line1;
                    String script = "";
                    while ((line1 = textReader.readLine()) != null) {

                        if (line1.contains("//")) {
                            continue;
                        }

                        script += line1;
                    }

                    script = ChatColor.translateAlternateColorCodes('&', String.join("\n", script).replace("\n", "").replace("\t", ""));

                    scriptData.put(data, new ScriptObject(data, dir, script));
                    loaded = loaded + 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    public Collection<ScriptObject> getObjects(String key) {
        return scriptData.get(key);
    }
}
