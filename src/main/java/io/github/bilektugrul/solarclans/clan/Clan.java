package io.github.bilektugrul.solarclans.clan;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Clan {

    private static final SolarClans plugin = JavaPlugin.getPlugin(SolarClans.class);

    private final long ID;
    private final YamlConfiguration data;

    private String name, owner, creator;
    private boolean pvp;

    private final List<String> members = new ArrayList<>();
    private final List<Player> onlineMembers = new ArrayList<>();

    public Clan(YamlConfiguration data, long ID) {
        this.data = data;

        this.ID = ID;
    }

    public long getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getCreator() {
        return creator;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<Player> getOnlineMembers() {
        return onlineMembers;
    }

    public Clan setName(String name) {
        this.name = name;

        return this;
    }

    public Clan setCreator(String creator) {
        this.creator = creator;

        return this;
    }


    public Clan setOwner(String owner) {
        this.owner = owner;

        return this;
    }

    public Clan addMembers(List<String> members) {
        this.members.addAll(members);

        return this;
    }

    public Clan addMembers(String... members) {
        this.members.addAll(Arrays.asList(members));

        return this;
    }

    public void updateOnlineMembers() {
        onlineMembers.clear();

        for (Player loop : Bukkit.getOnlinePlayers()) {
            for (String member : members) {
                if (loop.getName().equalsIgnoreCase(member)) {
                    onlineMembers.add(loop);
                }
            }
        }
    }

    public String getCreationDate() {
        return Utils.millisToString(ID);
    }

    public boolean isPvPEnabled() {
        return pvp;
    }

    public void togglePvP() {
        this.pvp = !this.pvp;
    }

    public void save() throws IOException {
        data.set("ID", ID);
        data.set("name", name);
        data.set("owner", owner);
        data.set("creator", creator);
        data.set("members", members);

        data.save(new File(plugin.getDataFolder() + "/clans/" + ID + ".yml"));
    }

}