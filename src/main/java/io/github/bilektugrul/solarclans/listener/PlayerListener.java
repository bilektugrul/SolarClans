package io.github.bilektugrul.solarclans.listener;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.clan.Clan;
import io.github.bilektugrul.solarclans.clan.ClanManager;
import io.github.bilektugrul.solarclans.user.User;
import io.github.bilektugrul.solarclans.user.UserManager;
import io.github.bilektugrul.solarclans.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

public class PlayerListener implements Listener {

    private final UserManager userManager;
    private final ClanManager clanManager;

    public PlayerListener(SolarClans plugin) {
        this.userManager = plugin.getUserManager();
        this.clanManager = plugin.getClanManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        User user = userManager.loadUser(e.getPlayer());
        Clan clan = clanManager.getClan(user.getClanID());
        if (clan == null) return;

        clan.updateOnlineMembers();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) throws IOException {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);

        Clan clan = clanManager.getClan(user.getClanID());
        if (clan == null) return;

        clan.updateOnlineMembers();

        user.save();
        userManager.removeUser(user);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);

        if (user.isClanChat()) {
            e.setCancelled(true);

            if (!clanManager.isClanActive(user.getClanID())) {
                user.toggleClanChat();
                player.sendMessage(Utils.getMessage("clan-chat.clan-disabled", player));
                return;
            }


            String format = Utils.getMessage("clan-chat.format", player)
                    .replace("%message%", e.getMessage());
            clanManager.getClan(user.getClanID()).getOnlineMembers().forEach(member -> member.sendMessage(format));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player && e.getDamager() instanceof Player attacker) {
            User user = userManager.getUser(player);
            long clan = user.getClanID();
            User attackerUser = userManager.getUser(attacker);
            long attackerClan = attackerUser.getClanID();

            if (clan == attackerClan) {
                Clan c = clanManager.getClan(clan);
                e.setCancelled(!c.isPvPEnabled());
            }
        }
    }

}