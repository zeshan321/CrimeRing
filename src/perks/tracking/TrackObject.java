package perks.tracking;

import org.bukkit.Location;

import java.util.UUID;

public class TrackObject {

    public UUID tracking;
    public String name;
    public Location lastLocation;

    public TrackObject(UUID tracking, String name, Location lastLocation) {
        this.tracking = tracking;
        this.name = name;
        this.lastLocation = lastLocation;
    }
}
