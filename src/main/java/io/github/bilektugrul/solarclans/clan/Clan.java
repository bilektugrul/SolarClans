package io.github.bilektugrul.solarclans.clan;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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

    private int kills = 0;
    private Inventory vaultInventory;

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

    public Clan setKills(int kills) {
        this.kills = kills;

        return this;
    }

    public Clan setPvP(boolean pvp) {
        this.pvp = pvp;

        return this;
    }

    public void setVaultInventory(Inventory vaultInventory) {
        this.vaultInventory = vaultInventory;
    }

    public Inventory getVaultInventory() {
        return vaultInventory;
    }

    public void addKill() {
        kills++;
    }

    public int getKills() {
        return kills;
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

    public void sendMessage(String message) {
        for (Player player : onlineMembers) {
            player.sendMessage(message);
        }
    }

    public String getCreationDate() {
        return Utils.millisToString(ID);
    }

    public YamlConfiguration getData() {
        return data;
    }

    public boolean isPvPEnabled() {
        return pvp;
    }

    public void togglePvP() {
        this.pvp = !this.pvp;
    }

    public boolean isDisbanded() {
        return data.getBoolean("disbanded");
    }

    public void save() throws IOException {
        data.set("ID", ID);
        data.set("name", name);
        data.set("creator", creator);
        data.set("owner", owner);
        data.set("pvp", pvp);
        data.set("kills", kills);
        data.set("members", members);

        if (isDisbanded()) data.save(new File(plugin.getDataFolder() + "/clans/" + ID + "-disbanded.yml"));
        else data.save(new File(plugin.getDataFolder() + "/clans/" + ID + ".yml"));
    }

}