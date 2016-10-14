package perks.cop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CopUtil {

    public List<Player> getCops() {
        return Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("CR.cop")).collect(Collectors.toList());
    }
}
