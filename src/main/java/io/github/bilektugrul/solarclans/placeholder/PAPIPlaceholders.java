package io.github.bilektugrul.solarclans.placeholder;

import com.avaje.ebean.validation.NotNull;
import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.leaderboard.BalanceLeaderboard;
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

            if (place >= BalanceLeaderboard.clanBalanceLeaderboard.size()) return "";

            if (identifier.endsWith("name")) {
                return BalanceLeaderboard.clanBalanceLeaderboard.get(place).getName();
            } else if (identifier.endsWith("balance")) {
                return Utils.moneyWithCommas(BalanceLeaderboard.clanBalanceLeaderboard.get(place).getValue());
            }

            return "";
        }

        User user = (User) player.getMetadata("clans-user");

        if (identifier.equalsIgnoreCase("clan")) {
            if (!user.hasClan()) return "";

            return user.getClan().getName();
        }

        return "";
    }

}
