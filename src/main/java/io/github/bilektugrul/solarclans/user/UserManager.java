package io.github.bilektugrul.solarclans.user;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.clan.ClanManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UserManager {

    private final SolarClans plugin;
    private final ClanManager clanManager;

    private final Set<User> userList = new HashSet<>();

    public UserManager(SolarClans plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    public User loadUser(Player p) {
        return loadUser(p, true);
    }

    public User loadUser(Player p, boolean keep) {
        String name = p.getName();
        YamlConfiguration dataFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + "/players/" + name + ".yml"));
        User user = new User(dataFile, name);
        if (keep) {
            userList.add(user);
            p.setMetadata("clans-user", new FixedMetadataValue(plugin, user));
        }
        return user;
    }

    public User getUser(Player p) {
        return (User) p.getMetadata("clans-user").get(0).value();
    }

    public User getUser(String name) {
        for (User user : userList) {
            if (user.getName().equalsIgnoreCase(name)) {
                return user;
            }
        }

        return null;
    }

    public boolean isLoaded(String name) {
        return getUser(name) != null;
    }

    public void removeUser(User user) {
        userList.remove(user);
    }

    public Set<User> getUserList() {
        return new HashSet<>(userList);
    }

    public void saveUsers() throws IOException {
        for (User user : userList) {
            user.save();
        }
    }

}