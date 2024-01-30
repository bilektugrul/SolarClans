package io.github.bilektugrul.solarclans.clan;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.user.User;
import io.github.bilektugrul.solarclans.user.UserManager;
import io.github.bilektugrul.solarclans.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClanManager {

    private final SolarClans plugin;
    private UserManager userManager;

    private final Set<Clan> clans = new HashSet<>();

    public ClanManager(SolarClans plugin) {
        this.plugin = plugin;

        loadClans();
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void loadClans() {
        this.clans.clear();

        File[] clanFiles = new File(plugin.getDataFolder() + "/clans/").listFiles();
        if (clanFiles == null) {
            return;
        }

        for (File clanFile : clanFiles) {
            loadClan(YamlConfiguration.loadConfiguration(clanFile));
        }
    }

    public void loadClan(YamlConfiguration clanFile) {
        long id = clanFile.getLong("ID");

        String name = clanFile.getString("name");
        String owner = clanFile.getString("owner");
        List<String> members = clanFile.getStringList("members");

        Clan clan = new Clan(clanFile, id)
                .setName(name)
                .addMembers(members)
                .setOwner(owner);
        clans.add(clan);
    }

    public void saveClans() throws IOException {
        for (Clan clan : clans) {
            clan.save();
        }
    }

    public void createClan(String name, Player owner) {
        long id = System.nanoTime();

        YamlConfiguration data = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + "/clans/" + name + ".yml"));
        Clan clan = new Clan(data, id)
                .setName(name)
                .addMembers(owner.getName())
                .setOwner(owner.getName());
        userManager.getUser(owner).setClanID(id);
        clans.add(clan);
        clan.updateOnlineMembers();
    }

    public void joinClan(Player player, Clan clan) {
        User user = userManager.getUser(player);
        user.setClanID(clan.getID());
        user.removeInvitation(clan.getID());

        player.sendMessage(Utils.getMessage("joined-clan", player).replace("%clan%", clan.getName()));
        for (Player onlineMember : clan.getOnlineMembers()) {
            onlineMember.sendMessage(Utils.getMessage("member-join", onlineMember).replace("%member%", player.getName()));
        }

        clan.getMembers().add(player.getName());
        clan.updateOnlineMembers();
    }

    public void leaveClan(Player player) {
        User user = userManager.getUser(player);
        Clan clan = getClan(user.getClanID());
        user.setClanID(-1);

        player.sendMessage(Utils.getMessage("you-left", player));
        clan.getMembers().remove(player.getName());
        clan.updateOnlineMembers();

        for (Player onlineMember : clan.getOnlineMembers()) {
            onlineMember.sendMessage(Utils.getMessage("member-left", onlineMember).replace("%member%", player.getName()));
        }

    }

    public void disbandClan(long clanID) {
        disbandClan(getClan(clanID));
    }

    public void disbandClan(Clan clan) {
        for (String playerName : clan.getMembers()) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                userManager.getUser(player).setClanID(-1);
                player.sendMessage(Utils.getMessage("disbanded", player).replace("%owner%", clan.getOwner()));
            }
        }

        clans.remove(clan);
    }

    public boolean isClanActive(long clanID) {
        for (Clan clan : clans) {
            if (clan.getID() == clanID) {
                return true;
            }
        }

        return false;
    }

    public Clan getClan(long clanID) {
        for (Clan clan : clans) {
            if (clan.getID() == clanID) {
                return clan;
            }
        }

        return null;
    }

    public Clan getClan(String clanName) {
        for (Clan clan : clans) {
            if (clan.getName().equalsIgnoreCase(clanName)) {
                return clan;
            }
        }

        return null;
    }

}
