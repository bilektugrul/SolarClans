package io.github.bilektugrul.solarclans.leaderboard;

import io.github.bilektugrul.solarclans.SolarClans;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BalanceLeaderboard {

    private static final SolarClans plugin = JavaPlugin.getPlugin(SolarClans.class);
    private static final Economy economy = plugin.getVaultManager().getEconomy();

    public static List<LeaderboardEntry> clanBalanceLeaderboard = new ArrayList<>();

    public static void reloadLeaderboard() {
        File base = new File(plugin.getDataFolder() + "/clans/");
        File[] clanFiles = base.listFiles();
        if (clanFiles == null) return;

        clanBalanceLeaderboard.clear();

        for (File clanFile : clanFiles) {
            if (clanFile.getName().contains("-disbanded")) continue;

            FileConfiguration data = YamlConfiguration.loadConfiguration(clanFile);

            long totalBalance = 0;
            String name = data.getString("name");
            List<String> members = data.getStringList("members");

            for (String member : members) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member);
                if (offlinePlayer == null) {
                    System.out.println("Member " + member + " of clan " + data.getString("name") + " could not be found.");
                    continue;
                }

                totalBalance += (long) economy.getBalance(offlinePlayer);
            }

            clanBalanceLeaderboard.add(new LeaderboardEntry(name, totalBalance));
        }

        sort();
    }

    private static void sort() {
        clanBalanceLeaderboard = clanBalanceLeaderboard
                .stream()
                .sorted(Comparator.comparingLong(LeaderboardEntry::value).reversed())
                .collect(Collectors.toList());
    }

}