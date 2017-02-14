package perks;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PerksUtil {

    public boolean hasPermission(Player player, String string) {
        return player.hasPermission(new Permission(string, PermissionDefault.FALSE));
    }

}
