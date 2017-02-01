package perks.arrest;

import java.util.HashMap;

public class TimerObject {

    public long timestamp;
    public int seconds;
    public HashMap<String, Integer> crimes;

    public TimerObject(long timestamp, int seconds) {
        this.timestamp = timestamp;
        this.seconds = seconds;
        this.crimes = new HashMap<>();
    }

    public void addCrime(PerkManager.Crime crime) {
        int amount = 1;

        if (crimes.containsKey(crime.name())) {
            amount = crimes.get(crime.name()) + 1;
        }

        crimes.put(crime.name(), amount);
    }
}
