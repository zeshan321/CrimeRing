package perks;

public class CooldownObject {

    public String type;
    public int seconds;
    public long timestamp;

    public CooldownObject(String type, int seconds, long timestamp) {
        this.type = type;
        this.seconds = seconds;
        this.timestamp = timestamp;
    }
}
