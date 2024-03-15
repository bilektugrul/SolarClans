package io.github.bilektugrul.solarclans.clan;

import com.hakan.core.HCore;
import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.user.User;
import io.github.bilektugrul.solarclans.user.UserManager;
import io.github.bilektugrul.solarclans.util.Utils;
import me.despical.commons.number.NumberUtils;
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
    private final UserManager userManager;

    private final Set<Clan> clans = new HashSet<>();

    public ClanManager(SolarClans plugin) {
        this.plugin = plugin;
        this.userManager = new UserManager(plugin);
        this.plugin.setUserManager(userManager);
    }

    public void loadClans() {
        this.clans.clear();

        File[] clanFiles = new File(plugin.getDataFolder() + "/clans/").listFiles();
        if (clanFiles == null) {
            return;
        }

        for (File clanFile : clanFiles) {
            if (clanFile.getName().contains("-disbanded")) {
                continue;
            }

            loadClan(YamlConfiguration.loadConfiguration(clanFile));
        }
    }

    public Clan getDisbandedClan(String clanID) {
        File[] clanFiles = new File(plugin.getDataFolder() + "/clans/").listFiles();
        if (clanFiles == null) {
            return null;
        }

        if (NumberUtils.isLong(clanID)) {
            for (File clanFile : clanFiles) {
                if (clanFile.getName().contains(clanID + "-disbanded")) {
                    return loadClan(YamlConfiguration.loadConfiguration(clanFile), false);
                }
            }

            return null;
        }

        for (File clanFile : clanFiles) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(clanFile);
            if (yaml.getString("name").equalsIgnoreCase(clanID)) {
                return loadClan(yaml, false);
            }
        }

        return null;
    }

    public void loadClan(YamlConfiguration clanFile) {
        loadClan(clanFile, true);
    }

    public Clan loadClan(YamlConfiguration clanFile, boolean keep) {
        long id = clanFile.getLong("ID");

        String name = clanFile.getString("name");
        String owner = clanFile.getString("owner");
        String creator = clanFile.getString("creator");
        List<String> members = clanFile.getStringList("members");
        int kills = clanFile.getInt("kills");
        boolean pvp = clanFile.getBoolean("pvp");

        Clan clan = new Clan(clanFile, id)
                .setName(name)
                .addMembers(members)
                .setOwner(owner)
                .setCreator(creator)
                .setPvP(pvp)
                .setKills(kills)
                .updateWeeklyKills();
        if (keep) {
            clans.add(clan);
            clan.updateOnlineMembers();
            clan.updateUserInfo();
        }

        return clan;
    }

    public void saveClans() throws IOException {
        for (Clan clan : clans) {
            clan.save();
        }
    }

    public void createClan(String name, Player owner) {
        long id = System.currentTimeMillis();
        if (getClan(id) != null) {
            HCore.syncScheduler()
                    .after(5)
                    .run(() -> createClan(name, owner));
            return;
        }

        YamlConfiguration data = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + "/clans/" + name + ".yml"));
        Clan clan = new Clan(data, id)
                .setName(name)
                .addMembers(owner.getName())
                .setCreator(owner.getName())
                .setOwner(owner.getName())
                .setPvP(true)
                .setKills(0)
                .updateWeeklyKills();
        clans.add(clan);
        userManager.getUser(owner).setClanID(id);
        clan.updateOnlineMembers();
    }

    public void joinClan(Player player, Clan clan) {
        clan.getMembers().add(player.getName());
        User user = userManager.getUser(player);
        user.setClanID(clan.getID());
        user.removeInvitation(clan.getID());

        player.sendMessage(Utils.getMessage("joined-clan", player).replace("%clan%", clan.getName()));
        for (Player onlineMember : clan.getOnlineMembers()) {
            onlineMember.sendMessage(Utils.getMessage("member-join", onlineMember).replace("%member%", player.getName()));
        }

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
            onlineMember.sendMessage(Utils.getMessage("member-left", onlineMember)
                    .replace("%member%", player.getName()));
        }

    }

    public void kickFromClan(Clan clan, String name) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            player.sendMessage(Utils.getMessage("got-kicked", player));
            userManager.getUser(name).setClanID(-1);
            if (player.hasMetadata("clans-vault-open")) {
                player.closeInventory();
                player.removeMetadata("clans-vault-open", plugin);
            }
        }

        clan.getMembers().remove(name);
        clan.updateOnlineMembers();

        for (Player onlineMember : clan.getOnlineMembers()) {
            onlineMember.sendMessage(Utils.getMessage("member-kicked", onlineMember)
                    .replace("%member%", name));
        }
    }

    public void changeLeader(long clanID, Player player) {
        changeLeader(clanID, player, false);
    }

    public void changeLeader(long clanID, Player player, boolean silent) {
        Clan clan = getClan(clanID);

        String oldOwner = clan.getOwner();
        clan.setOwner(player.getName());
        if (!silent) {
            player.sendMessage(Utils.getMessage("you-own-now", player));

            for (Player onlineMember : clan.getOnlineMembers()) {
                onlineMember.sendMessage(Utils.getMessage("owner-changed", onlineMember)
                        .replace("%new%", player.getName())
                        .replace("%old%", oldOwner));
            }
        }
    }

    public void changeName(Clan clan, String newName) {
        String oldName = clan.getName();
        clan.setName(newName);

        for (Player onlineMember : clan.getOnlineMembers()) {
            onlineMember.sendMessage(Utils.getMessage("name-changed", onlineMember)
                    .replace("%new%", newName)
                    .replace("%old%", oldName));
        }
    }

    public void disbandClan(long clanID) {
        disbandClan(getClan(clanID));
    }

    public void disbandClan(Clan clan) {
        disbandClan(clan, false);
    }

    public void disbandClan(Clan clan, boolean silent) {
        for (String playerName : clan.getMembers()) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                userManager.getUser(player).setClanID(-1);
                if (player.hasMetadata("clans-vault-open")) {
                    player.closeInventory();
                    player.removeMetadata("clans-vault-open", plugin);
                }

                if (!silent) player.sendMessage(Utils.getMessage("disbanded", player).replace("%owner%", clan.getOwner()));
            }
        }

        try {
            clan.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        clan.getData().set("disbanded", true);
        File clanFile = new File(plugin.getDataFolder() + "/clans/" + clan.getID() + ".yml");
        File newClanFile = new File(plugin.getDataFolder() + "/clans/" + clan.getID() + "-disbanded.yml");
        clanFile.renameTo(newClanFile);
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
