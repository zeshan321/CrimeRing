package script;

import com.zeshanaslam.crimering.FileHandler;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class ScriptsManager {

    public HashMap<String, ScriptObject> scriptData = new HashMap<>();

    public void load() {
        scriptData.clear();

        FileHandler imports = new FileHandler("plugins/CrimeRing/scripts.yml");

        int loaded = 0;

        for (String data : imports.getKeys()) {
            String dir = imports.getString(data + ".dir");

            try {
                scriptData.put(data, new ScriptObject(data, dir, ChatColor.translateAlternateColorCodes('&', String.join("\n", Files.readAllLines(Paths.get("plugins/CrimeRing/scripts/" + File.separator + dir))).replace("\n", "").replace("\t", ""))));
            } catch (IOException e) {
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
