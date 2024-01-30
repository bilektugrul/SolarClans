package io.github.bilektugrul.solarclans.user;

import io.github.bilektugrul.solarclans.SolarClans;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class User {

    private static final SolarClans plugin = JavaPlugin.getPlugin(SolarClans.class);

    private final YamlConfiguration data;
    private final String name;

    private long clanID = -1;
    private final List<Long> clanInvitations = new ArrayList<>();

    public User(YamlConfiguration data, String name) {
        this.data = data;
        this.name = name;

        if (data.isSet("clanID")) {
            this.clanID = data.getLong("clanID");

            if (!plugin.getClanManager().isClanActive(clanID)) {
                this.clanID = -1;
                data.set("clanID", clanID);
            }
        }

        data.set("lastKnownName", name);
    }

    public String getName() {
        return name;
    }

    public long getClanID() {
        return clanID;
    }

    public void setClanID(long clanID) {
        this.clanID = clanID;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public boolean hasClan() {
        return this.clanID != -1;
    }

    public boolean isClanOwner() {
        if (!hasClan()) {
            return false;
        }

        return plugin.getClanManager().getClan(clanID).getOwner().equalsIgnoreCase(name);
    }

    public boolean isInvited(long clanID) {
        return clanInvitations.contains(clanID);
    }

    public void addInvitation(long clanID) {
        clanInvitations.add(clanID);
    }

    public void removeInvitation(long clanID) {
        clanInvitations.remove(clanID);
    }

    public void save() throws IOException {
        data.set("clanID", clanID);

        data.save(new File(plugin.getDataFolder() + "/players/" + name + ".yml"));
    }

}