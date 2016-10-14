package perks;

public class TimerObject {

    public long timestamp;
    public int seconds;
    public String type;

    public TimerObject(long timestamp, int seconds, String type) {
        this.timestamp = timestamp;
        this.seconds = seconds;
        this.type = type;
    }
}
