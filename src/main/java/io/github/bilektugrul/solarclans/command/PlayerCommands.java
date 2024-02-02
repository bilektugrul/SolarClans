package io.github.bilektugrul.solarclans.command;

import com.hakan.core.HCore;
import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.clan.Clan;
import io.github.bilektugrul.solarclans.leaderboard.BalanceLeaderboard;
import io.github.bilektugrul.solarclans.leaderboard.KillLeaderboard;
import io.github.bilektugrul.solarclans.leaderboard.LeaderboardEntry;
import io.github.bilektugrul.solarclans.user.User;
import io.github.bilektugrul.solarclans.util.Utils;
import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PlayerCommands extends AbstractCommand {


    public PlayerCommands(SolarClans plugin) {
        super(plugin);
    }

    @Command(
            name = "clan",
            aliases = {"c", "clans", "c.help", "clans.help", "clan.help"},
            desc = "Clans main command"
    )
    public void mainCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();

        long cost = Utils.getLong("new-clan-cost");
        sender.sendMessage(Utils.getMessage("main-command", sender)
                .replace("%cost%", String.valueOf(cost)));
    }

    private final Set<Player> createConfirmWaiting = new HashSet<>();

    @Command(
            name = "clan.create",
            aliases = {"c.create", "c.c", "clans.c", "clans.create"},
            desc = "Clans main command",
            min = 1,
            max = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void createCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        int required = Utils.getInt("new-clan-cost");
        int min = Utils.getInt("minimum-name-length");
        int max = Utils.getInt("maximum-name-length");

        if (!economy.has(player, required)) {
            double current = economy.getBalance(player);
            player.sendMessage(Utils.getMessage("not-enough-balance", player)
                    .replace("%remaining%", String.valueOf(required - current)));
            return;
        }

        if (user.hasClan()) {
            player.sendMessage(Utils.getMessage("already-have-clan", player));
            return;
        }

        String clanName = arguments.getArgument(0);
        if (clanManager.getClan(clanName) != null) {
            player.sendMessage(Utils.getMessage("same-name", player));
            return;
        }

        if (clanName.length() < min) {
            player.sendMessage(Utils.getMessage("minimum-name-length", player).replace("%length%", String.valueOf(min)));
            return;
        }

        if (clanName.length() > max) {
            player.sendMessage(Utils.getMessage("maximum-name-length", player).replace("%length%", String.valueOf(max)));
            return;
        }

        if (createConfirmWaiting.contains(player)) {
            clanManager.createClan(clanName, player);
            player.sendMessage(Utils.getMessage("created", player)
                    .replace("%name%", clanName));
            createConfirmWaiting.remove(player);
            return;
        }

        createConfirmWaiting.add(player);
        HCore.syncScheduler()
                .after(5000, TimeUnit.MILLISECONDS)
                .run(() -> createConfirmWaiting.remove(player));
        player.sendMessage(Utils.getMessage("new-clan-confirm", player).replace("%cost%", String.valueOf(required)));
    }

    @Command(
            name = "clan.invite",
            aliases = {"c.invite", "c.inv", "clans.inv", "clans.invite"},
            min = 1,
            max = 1,
            desc = "Clans invite command",
            senderType = Command.SenderType.PLAYER
    )
    public void inviteCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);
        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        if (!user.isClanOwner()) {
            player.sendMessage(Utils.getMessage("not-the-owner", player));
            return;
        }

        long clanID = user.getClanID();
        Clan clan = clanManager.getClan(clanID);
        if (clan.getMembers().size() == 5) {
            player.sendMessage(Utils.getMessage("clan-full", player));
            return;
        }

        String toInvite = arguments.getArgument(0);
        if (toInvite.equalsIgnoreCase(user.getName())) {
            player.sendMessage(Utils.getMessage("self-invite", player));
            return;
        }

        Player invited = Bukkit.getPlayer(toInvite);
        if (invited == null) {
            player.sendMessage(Utils.getMessage("player-not-found", player));
            return;
        }

        User invitedUser = userManager.getUser(invited);
        if (invitedUser.isInvited(clanID)) {
            player.sendMessage(Utils.getMessage("already-invited", player)
                    .replace("%invited%", invited.getName()));
            return;
        }

        if (invitedUser.hasClan()) {
            player.sendMessage(Utils.getMessage("invited-have-clan", player)
                    .replace("%invited%", invited.getName()));
            return;
        }

        invitedUser.addInvitation(clanID);

        player.sendMessage(Utils.getMessage("sent-invite", player).replace("%invited%", invited.getName()));

        invited.sendMessage(Utils.getMessage("new-invite", invited)
                .replace("%clan%", clan.getName())
                .replace("%inviter%", player.getName()));

        HCore.syncScheduler()
                .after(20000, TimeUnit.MILLISECONDS)
                .run(() -> {
                    if (invitedUser.hasClan()) {
                        invitedUser.removeInvitation(clanID);
                    }

                    if (invitedUser.isInvited(clanID)) {
                        invitedUser.removeInvitation(clanID);
                        invited.sendMessage(Utils.getMessage("invite-expired", invited).replace("%clan%", clan.getName()));
                        player.sendMessage(Utils.getMessage("sent-invite-expired", invited).replace("%invited%", invited.getName()));
                    }}
                );
    }

    @Command(
            name = "clan.join",
            aliases = {"c.join", "c.j", "clans.join", "clans.j"},
            min = 1,
            max = 1,
            desc = "Clans join command",
            senderType = Command.SenderType.PLAYER
    )
    public void joinCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (user.hasClan()) {
            player.sendMessage(Utils.getMessage("already-have-clan", player));
            return;
        }

        String clanName = arguments.getArgument(0);
        Clan clan = clanManager.getClan(clanName);
        if (clan == null) {
            player.sendMessage(Utils.getMessage("clan-not-available", player));
            return;
        }

        long clanID = clan.getID();
        if (!user.isInvited(clanID)) {
            player.sendMessage(Utils.getMessage("not-invited", player)
                    .replace("%clan%", clan.getName()));
            return;
        }

        clanManager.joinClan(player, clan);
    }

    private final Set<Player> leaveConfirmWaiting = new HashSet<>();

    @Command(
            name = "clan.leave",
            aliases = {"c.leave", "c.l", "clans.leave", "clans.l"},
            desc = "Clans leave command",
            senderType = Command.SenderType.PLAYER
    )
    public void leaveCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        if (user.isClanOwner()) {
            player.sendMessage(Utils.getMessage("own-clan", player));
            return;
        }

        if (leaveConfirmWaiting.contains(player)) {
            clanManager.leaveClan(player);
            leaveConfirmWaiting.remove(player);
            return;
        }

        leaveConfirmWaiting.add(player);
        HCore.syncScheduler()
                .after(5000, TimeUnit.MILLISECONDS)
                .run(() -> leaveConfirmWaiting.remove(player));

        player.sendMessage(Utils.getMessage("leave-confirm", player));
    }

    @Command(
            name = "clan.kick",
            aliases = {"c.kick", "c.k", "clans.k", "clans.kick"},
            desc = "Clans kick command",
            min = 1,
            max = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void kickCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        if (!user.isClanOwner()) {
            player.sendMessage(Utils.getMessage("not-the-owner", player));
            return;
        }

        String toKickStr = arguments.getArgument(0);
        if (toKickStr.equalsIgnoreCase(user.getName())) {
            player.sendMessage(Utils.getMessage("self-kick", player));
            return;
        }

        Player toKick = Bukkit.getPlayer(toKickStr);
        if (toKick == null) {
            player.sendMessage(Utils.getMessage("player-not-found", player));
            return;
        }

        User toKickUser = userManager.getUser(toKick);
        long ownClan = user.getClanID();
        long toKickClan = toKickUser.getClanID();

        if (ownClan != toKickClan) {
            player.sendMessage(Utils.getMessage("not-same-clan", player));
            return;
        }

        clanManager.kickFromClan(toKick, toKickUser);
        player.sendMessage(Utils.getMessage("kicked-member", player).replace("%kicked%", toKick.getName()));

    }

    private final Set<Player> disbandConfirmWaiting = new HashSet<>();

    @Command(
            name = "clan.disband",
            aliases = {"c.disband", "c.d", "clans.disband", "clans.d"},
            desc = "Clans disband command",
            senderType = Command.SenderType.PLAYER
    )
    public void disbandCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        if (!user.isClanOwner()) {
            player.sendMessage(Utils.getMessage("not-the-owner", player));
            return;
        }

        if (disbandConfirmWaiting.contains(player)) {
            clanManager.disbandClan(user.getClanID());
            disbandConfirmWaiting.remove(player);
            return;
        }

        disbandConfirmWaiting.add(player);
        HCore.syncScheduler()
                .after(5000, TimeUnit.MILLISECONDS)
                .run(() -> disbandConfirmWaiting.remove(player));

        player.sendMessage(Utils.getMessage("disband-confirm", player));
    }

    @Command(
            name = "clan.chat",
            aliases = {"c.chat", "c.ch", "clans.ch", "clans.chat"},
            desc = "Clans chat command",
            senderType = Command.SenderType.PLAYER
    )
    public void chatCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        user.toggleClanChat();
        player.sendMessage(Utils.getMessage("clan-chat." + user.isClanChat(), player));
    }

    @Command(
            name = "clan.info",
            aliases = {"c.info", "c.i", "clans.i", "clans.info"},
            desc = "Clans info command",
            senderType = Command.SenderType.PLAYER
    )
    public void infoCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        Clan clan = clanManager.getClan(user.getClanID());
        String infoMessage = Utils.getMessage("clan-info", player)
                .replace("%id%", String.valueOf(clan.getID()))
                .replace("%name%", clan.getName())
                .replace("%creator%", clan.getCreator())
                .replace("%owner%", clan.getOwner())
                .replace("%date%", clan.getCreationDate())
                .replace("%size%", String.valueOf(clan.getMembers().size()));

        long totalBalance = 0;

        for (String member : clan.getMembers()) {
            totalBalance += (long) economy.getBalance(Bukkit.getOfflinePlayer(member));
        }

        StringBuilder members = new StringBuilder();
        for (String member : clan.getMembers()) {
            members.append(member).append("\n");
        }
        infoMessage = infoMessage
                .replace("%members%", members)
                .replace("%kills%", String.valueOf(clan.getKills()))
                .replace("%balance%", Utils.moneyWithCommas(totalBalance));

        player.sendMessage(infoMessage);
    }

    private final Set<Player> changeLeaderConfirmWaiting = new HashSet<>();

    @Command(
            name = "clan.setleader",
            aliases = {"c.setleader", "c.sl", "clans.sl", "clans.setleader"},
            desc = "Clans set leader command",
            min = 1,
            max = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void setLeader(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        if (!user.isClanOwner()) {
            player.sendMessage(Utils.getMessage("not-the-owner", player));
            return;
        }

        String newLeader = arguments.getArgument(0);
        if (newLeader.equalsIgnoreCase(user.getName())) {
            player.sendMessage(Utils.getMessage("change-to-self", player));
            return;
        }

        Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
        if (newLeaderPlayer == null) {
            player.sendMessage(Utils.getMessage("player-not-found", player));
            return;
        }

        User newLeaderUser = userManager.getUser(newLeaderPlayer);
        long ownClan = user.getClanID();
        long toKickClan = newLeaderUser.getClanID();

        if (ownClan != toKickClan) {
            player.sendMessage(Utils.getMessage("not-same-clan", player));
            return;
        }

        if (changeLeaderConfirmWaiting.contains(player)) {
            clanManager.changeLeader(ownClan, newLeaderPlayer);
            changeLeaderConfirmWaiting.remove(player);
            return;
        }

        changeLeaderConfirmWaiting.add(player);
        HCore.syncScheduler()
                .after(5000, TimeUnit.MILLISECONDS)
                .run(() -> changeLeaderConfirmWaiting.remove(player));

        player.sendMessage(Utils.getMessage("change-leader-confirm", player)
                .replace("%leader%", newLeaderPlayer.getName()));
    }

    @Command(
            name = "clan.rename",
            aliases = {"c.rename", "c.rn", "clans.rn", "clans.rename"},
            desc = "Clans rename command",
            min = 1,
            max = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void renameCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        if (!user.isClanOwner()) {
            player.sendMessage(Utils.getMessage("not-the-owner", player));
            return;
        }

        Clan clan = clanManager.getClan(user.getClanID());
        String newName = arguments.getArgument(0);

        clanManager.changeName(clan, newName);
    }

    @Command(
            name = "clan.pvp",
            aliases = {"c.pvp", "c.p", "clans.p", "clans.pvp"},
            desc = "Clans pvp command",
            senderType = Command.SenderType.PLAYER
    )
    public void pvpToggleCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        if (!user.isClanOwner()) {
            player.sendMessage(Utils.getMessage("not-the-owner", player));
            return;
        }

        Clan clan = clanManager.getClan(user.getClanID());
        clan.togglePvP();
        for (Player onlineMember : clan.getOnlineMembers()) {
            onlineMember.sendMessage(Utils.getMessage("pvp-toggled.message", onlineMember));
            onlineMember.sendMessage(Utils.getMessage("pvp-toggled." + clan.isPvPEnabled(), onlineMember));
        }
    }

    @Command(
            name = "clan.top",
            aliases = {"c.top", "clans.top"},
            desc = "Clans leaderboard command",
            min = 1,
            max = 1,
            senderType = Command.SenderType.BOTH
    )
    public void leaderboardCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();
        String mode = arguments.getArgument(0);

        if (mode.equalsIgnoreCase("balance")) {
            StringBuilder balanceMessage = new StringBuilder(Utils.getMessage("leaderboard.balance.message", sender)).append('\n');
            String format = Utils.getMessage("leaderboard.balance.format", sender);

            int pos = 1;
            for (LeaderboardEntry entry : BalanceLeaderboard.clanBalanceLeaderboard) {
                balanceMessage.append(format
                        .replace("%position%", String.valueOf(pos++))
                        .replace("%clan%", entry.name())
                        .replace("%balance%", Utils.moneyWithCommas(entry.value())))
                        .append('\n');
                if (pos == 11) break;
            }

            sender.sendMessage(balanceMessage.toString());
            return;
        }

        if (mode.contains("kill")) {
            StringBuilder balanceMessage = new StringBuilder(Utils.getMessage("leaderboard.kill.message", sender)).append('\n');
            String format = Utils.getMessage("leaderboard.kill.format", sender);

            int pos = 1;
            for (LeaderboardEntry entry : KillLeaderboard.killLeaderboard) {
                balanceMessage.append(format
                                .replace("%position%", String.valueOf(pos++))
                                .replace("%clan%", entry.name())
                                .replace("%kills%", String.valueOf(entry.value())))
                        .append('\n');
                if (pos == 11) break;
            }

            sender.sendMessage(balanceMessage.toString());
        }
    }

}