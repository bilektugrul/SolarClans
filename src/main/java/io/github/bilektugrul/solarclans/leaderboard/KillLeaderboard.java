package io.github.bilektugrul.solarclans.leaderboard;

import io.github.bilektugrul.solarclans.SolarClans;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KillLeaderboard {

    private static final SolarClans plugin = JavaPlugin.getPlugin(SolarClans.class);

    public static List<LeaderboardEntry> killLeaderboard = new ArrayList<>();

    public static void reloadLeaderboard() {
        File base = new File(plugin.getDataFolder() + "/clans/");
        File[] clanFiles = base.listFiles();
        if (clanFiles == null) return;

        killLeaderboard.clear();

        for (File clanFile : clanFiles) {
            if (clanFile.getName().contains("-disbanded")) continue;

            FileConfiguration data = YamlConfiguration.loadConfiguration(clanFile);

            int kills = data.getInt("kills");
            String name = data.getString("name");

            killLeaderboard.add(new LeaderboardEntry(name, kills));
        }

        sort();
    }

    private static void sort() {
        killLeaderboard = killLeaderboard
                .stream()
                .sorted(Comparator.comparingLong(LeaderboardEntry::value).reversed())
                .collect(Collectors.toList());
    }

}