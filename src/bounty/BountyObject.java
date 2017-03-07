package bounty;

import java.util.List;
import java.util.UUID;

public class BountyObject {

    public String file;
    public UUID target;
    public UUID owner;
    public String details;
    public List<String> items;

    public BountyObject(String file, UUID target, UUID owner, String details, List<String> items) {
        this.file = file;
        this.target = target;
        this.owner = owner;
        this.details = details;
        this.items = items;
    }
}
