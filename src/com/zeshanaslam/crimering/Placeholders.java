package com.zeshanaslam.crimering;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import org.bukkit.entity.Player;

public class Placeholders {


    public Placeholders() {
        PlaceholderAPI.registerPlaceholder(Main.instance, "cr_player_bal", new PlaceholderReplacer() {
            @Override
            public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
                Player player = event.getPlayer();

                return String.valueOf(Main.instance.actionDefaults.getInvMoney(player));
            }
        });

        PlaceholderAPI.registerPlaceholder(Main.instance, "cr_player_class", new PlaceholderReplacer() {
            @Override
            public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
                Player player = event.getPlayer();
                String playerClass = "Citizen";

                if (!FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
                    return playerClass;
                }

                FileHandler fileHandler = new FileHandler("plugins/CrimeRing/player/" + player.getUniqueId().toString());
                if (fileHandler.contains("class")) {
                    playerClass = fileHandler.getString("class");

                    // Set correct color
                    if (playerClass.equals("Recruit") || playerClass.equals("Beat Cop") || playerClass.equals("Detective") || playerClass.equals("DEA Agent")) {
                        return "&b" + playerClass;
                    }

                    if (playerClass.equals("Corner Boy") || playerClass.equals("Dealer") || playerClass.equals("Supplier") || playerClass.equals("Trafficker")) {
                        return "&9" + playerClass;
                    }

                    if (playerClass.equals("Thug") || playerClass.equals("Enforcer") || playerClass.equals("Fixer") || playerClass.equals("Hitman")) {
                        return "&c" + playerClass;
                    }

                    if (playerClass.equals("Shoplifter") || playerClass.equals("Robber") || playerClass.equals("Burglar") || playerClass.equals("Thief")) {
                        return "&a" + playerClass;
                    }
                }

                return playerClass;
            }
        });

    }
}
