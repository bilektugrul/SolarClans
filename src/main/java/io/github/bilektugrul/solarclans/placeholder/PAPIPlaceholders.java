package io.github.bilektugrul.solarclans.placeholder;

import com.avaje.ebean.validation.NotNull;
import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.clan.Clan;
import io.github.bilektugrul.solarclans.leaderboard.BalanceLeaderboard;
import io.github.bilektugrul.solarclans.leaderboard.KillLeaderboard;
import io.github.bilektugrul.solarclans.user.User;
import io.github.bilektugrul.solarclans.user.UserManager;
import io.github.bilektugrul.solarclans.util.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PAPIPlaceholders extends PlaceholderExpansion {

    private final SolarClans plugin;
    private final UserManager userManager;

    public PAPIPlaceholders(SolarClans plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return Utils.listToString(plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "solarclans";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.contains("leaderboard_balance")) {
            String replaced = identifier.replace("leaderboard_balance", "");

            int place = Integer.parseInt(replaced.substring(replaced.indexOf("_") + 1, replaced.lastIndexOf("_")));
            place--;

            if (place >= BalanceLeaderboard.clanBalanceLeaderboard.size()) return "Empty";

            if (identifier.endsWith("name")) {
                return BalanceLeaderboard.clanBalanceLeaderboard.get(place).name();
            } else if (identifier.endsWith("balance")) {
                return Utils.moneyWithCommas(BalanceLeaderboard.clanBalanceLeaderboard.get(place).value());
            }

            return "Empty";
        }

        if (identifier.contains("leaderboard_kills")) {
            String replaced = identifier.replace("leaderboard_kills", "");

            int place = Integer.parseInt(replaced.substring(replaced.indexOf("_") + 1, replaced.lastIndexOf("_")));
            place--;

            if (place >= KillLeaderboard.killLeaderboard.size()) return "Empty";

            if (identifier.endsWith("name")) {
                return KillLeaderboard.killLeaderboard.get(place).name();
            } else if (identifier.endsWith("kills")) {
                return String.valueOf(KillLeaderboard.killLeaderboard.get(place).value());
            }

            return "Empty";
        }

        if (identifier.contains("leaderboard_weekly")) {
            String replaced = identifier.replace("leaderboard_weekly", "");

            int place = Integer.parseInt(replaced.substring(replaced.indexOf("_") + 1, replaced.lastIndexOf("_")));
            place--;

            if (place >= KillLeaderboard.weeklyKillLeaderboard.size()) return "Empty";

            if (identifier.endsWith("name")) {
                return KillLeaderboard.weeklyKillLeaderboard.get(place).name();
            } else if (identifier.endsWith("kills")) {
                return String.valueOf(KillLeaderboard.weeklyKillLeaderboard.get(place).value());
            }

            return "Empty";
        }

        User user = userManager.getUser(player);
        if (!user.hasClan()) return "";

        Clan clan = user.getClan();
        if (identifier.equalsIgnoreCase("clan")) {
            return clan.getName();
        }

        if (identifier.equalsIgnoreCase("members")) {
            return String.valueOf(clan.getMembers().size());
        }

        if (identifier.equalsIgnoreCase("online_members")) {
            return String.valueOf(clan.getOnlineMembers().size());
        }

        if (identifier.equalsIgnoreCase("owner")) {
            return clan.getOwner();
        }

        if (identifier.equalsIgnoreCase("creator")) {
            return clan.getCreator();
        }

        if (identifier.equalsIgnoreCase("kills")) {
            return String.valueOf(clan.getKills());
        }

        return "";
    }

}
