package damage;

import com.zeshanaslam.crimering.FileHandler;

import java.util.HashMap;

public class DamageManager {

    public HashMap<String, Double> damages = new HashMap<>();

    public void load() {
        damages.clear();

        FileHandler imports = new FileHandler("plugins/CrimeRing/damage.yml");

        for (String s : imports.getStringList("Override")) {
            String[] data = s.split(" = ");

            damages.put(data[0], Double.parseDouble(data[1]));
        }
    }
}
