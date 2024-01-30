package io.github.bilektugrul.solarclans.util;

import io.github.bilektugrul.solarclans.SolarClans;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {

    private static final SolarClans plugin = JavaPlugin.getPlugin(SolarClans.class);
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,###.#");

    public static final DateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");

    public static FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public static int getInt(String path) {
        return plugin.getConfig().getInt(path);
    }

    public static long getLong(String path) {
        return plugin.getConfig().getLong(path);
    }

    public static String getString(String string) {
        return plugin.getConfig().getString(string);
    }

    public static Boolean getBoolean(String string) {
        return plugin.getConfig().getBoolean(string);
    }

    public static List<String> getStringList(String string) {
        return plugin.getConfig().getStringList(string);
    }

    public static String getMessage(String msg, CommandSender sender) {
        String message = listToString(colored(getStringList("messages." + msg)));
        if (sender instanceof Player player) {
            message = message.replace("%player%", player.getName());
        }

        return message.replace("%prefix%", colored(getString("prefix")));
    }

    public static String colored(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> colored(List<String> strings) {
        List<String> list = new ArrayList<>();
        for (String str : strings) {
            list.add(ChatColor.translateAlternateColorCodes('&', str));
        }
        return list;
    }

    public static String arrayToString(String[] array) {
        return String.join(" ", array);
    }

    public static String listToString(List<String> list) {
        return String.join("\n", list);
    }

    public static String fileToString(File file) throws IOException {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        List<String> content = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null)
            content.add(line);

        return listToString(content);
    }

    public static String millisToString(long millis) {
        Date date = new Date(millis);
        return dateFormat.format(date);
    }

    public static String moneyWithCommas(long l) {
        return decimalFormat.format(l);
    }

    public static void sendMessage(String msg, CommandSender sendTo) {
        String message = getMessage("messages." + msg, sendTo);
        sendTo.sendMessage(message);
    }

}