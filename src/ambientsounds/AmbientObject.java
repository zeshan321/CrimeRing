package ambientsounds;

public class AmbientObject {

    public long timestamp;
    public int seconds;
    public String type;

    public AmbientObject(long timestamp, int seconds, String type) {
        this.timestamp = timestamp;
        this.seconds = seconds;
        this.type = type;
    }
}
