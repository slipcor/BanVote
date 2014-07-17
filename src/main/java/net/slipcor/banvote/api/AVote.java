package net.slipcor.banvote.api;

import net.slipcor.banvote.BanVotePlugin;
import net.slipcor.banvote.util.AdvanceRunner;
import net.slipcor.banvote.util.Config;
import net.slipcor.banvote.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public abstract class AVote {
    public static enum voteState {
        MUTETARGET, MUTEVOTER, POSITIVE, NEGATIVE, NULL
    }


    protected final String type;
    protected voteState state;
    protected final String voter;
    protected final String target;
    protected BukkitTask runner = null;
    protected boolean half = false;
    protected String reason;

    protected final Set<String> yes = new HashSet<String>();
    protected final Set<String> nope = new HashSet<String>();

    /**
     * Creates a vote class instance, announces the vote and starts its timer
     *
     * @param pTarget the player that is subject of a possible ban
     * @param player  the player that initiated the ban
     * @param sReason the reason given for banning
     */
    public AVote(final Player pTarget, final Player player, final String sReason, final byte bType) {
        voter = player.getName();
        target = pTarget == null ? "null" : pTarget.getName();
        state = voteState.MUTETARGET;

        type = parse(bType);

        final int interval = 20 * Math.round(Config.stageSeconds / 2); // half a minute
        BanVotePlugin.debug.info(type + "Vote interval: " + interval + " ticks");

        reason = sReason;

        runner = Bukkit
                .getServer()
                .getScheduler()
                .runTaskTimer(BanVotePlugin.instance,
                        new AdvanceRunner(this), interval, interval);
    }

    protected String commandReplace(final String command, final String sBanTarget, final int minutes) {
        /*
         *
		 * 
		 * # - $w the player being the result winner # - $l the player being the
		 * result loser # - $m the result time calculated to minutes # - $h the
		 * result time calculated to hours # - $s the result time calculated to
		 * seconds # - $fm the result time, floored minutes # - $fh the result
		 * time, floored hours
		 * $r the reason
		 */
        String cmd = command;
        cmd = cmd.replace("$w", (voter.equals(sBanTarget) ? target : voter));
        cmd = cmd.replace("$l", sBanTarget);

        cmd = cmd.replace("$m", String.valueOf(minutes));
        cmd = cmd.replace("$h", String.valueOf(minutes / 60));
        cmd = cmd.replace("$s", String.valueOf(minutes * 60));

        cmd = cmd.replace("$fm", String.valueOf(minutes % 60));
        cmd = cmd.replace("$fh", String.valueOf(Math.floor(minutes / 60)));
        reason = reason.equals("null") ? "" : reason;
        cmd = cmd.replace("$r", reason);

        return cmd;
    }

    /**
     * commit a negative vote
     *
     * @param player the voting player
     */
    public void commitNoVote(final Player player) {
        BanVotePlugin.debug.info("player " + player.getName() + " votes NO");
        if (!mayVote(player.getName())) {
            BanVotePlugin.instance.msg(player, Language.INFO_ALREADYVOTED.toString());
            return;
        }
        nope.add(player.getName());
        BanVotePlugin.instance.msg(player, Language.BAD_VOTED.toString());
    }

    /**
     * commit a positive vote
     *
     * @param player the voting player
     */
    public void commitYesVote(final Player player) {
        BanVotePlugin.debug.info("player " + player.getName() + " votes YES");
        if (!mayVote(player.getName())) {
            BanVotePlugin.instance.msg(player, Language.INFO_ALREADYVOTED.toString());
            return;
        }
        yes.add(player.getName());
        BanVotePlugin.instance.msg(player, Language.GOOD_VOTED.toString());
    }

    /**
     * hand over vote state
     *
     * @return state enum
     */
    public voteState getState() {
        return state;
    }

    /**
     * calculate afk players
     *
     * @return hashset of all afk players
     */
    public Set<String> getAfk() {
        final Set<String> afk = new HashSet<String>();
    /*

        try {
            if (BanVotePlugin.instance.getServer().getPluginManager()
                    .getPlugin("SimpleAFK") == null) {
                return afk;

            final SimpleAFK plugin = (SimpleAFK) BanVotePlugin.instance.getServer()
                    .getPluginManager().getPlugin("SimpleAFK");

            for (Player p : plugin.afkPlayers.keySet()) {
                if (yes.contains(p.getName())) {
                    continue;
                }
                if (nope.contains(p.getName())) {
                    continue;
                }
                afk.add(p.getName());
            }
        } catch (Exception e) {

        }
*/
        return afk;
    }

    /**
     * return a string containing all content of a given hashset joined with a
     * space
     *
     * @param set the hashset to join
     * @return a string with all entries
     */
    public String getNames(final Set<String> set) {
        String result = "";
        for (String s : set) {
            result += (result.equals("")) ? s : (", " + s);
        }
        return result;
    }

    /**
     * construct a hashset based on players not in any other hashset
     *
     * @param afk the afk hashset
     * @return the hashset of players not voted and not afk
     */
    public Set<String> getNon(final Set<String> afk) {
        final Set<String> non = new HashSet<String>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (yes.contains(p.getName())) {
                continue;
            }
            if (nope.contains(p.getName())) {
                continue;
            }
            if (afk.contains(p.getName())) {
                continue;
            }
            BanVotePlugin.debug.info("getNon - adding: " + p.getName());
            non.add(p.getName());
        }
        return non;
    }

    /**
     * hand over the target
     *
     * @return target player name
     */
    public String getTarget() {
        return target;
    }

    /**
     * hand over the type
     *
     * @return vote type
     */
    public String getType() {
        return type;
    }

    /**
     * hand over voting player name
     *
     * @return voting player name
     */
    public String getVoter() {
        return voter;
    }

    /**
     * check if a player may vote
     *
     * @param sPlayer the player name to check
     * @return true if a player may vote, false otherwise
     */
    private boolean mayVote(final String sPlayer) {
        BanVotePlugin.debug.info(yes.contains(sPlayer) + " || "
                + nope.contains(sPlayer) + " || " + voter.equals(sPlayer)
                + " || " + target.equals(sPlayer));
        return !(yes.contains(sPlayer) || nope.contains(sPlayer)
                || voter.equals(sPlayer) || target.equals(sPlayer));
    }

    /**
     * try to commit a vote
     *
     * @param sVote  vote value
     * @param player the player trying to vote
     */
    public static void commit(final String sVote, final Player player) {
        BanVotePlugin.debug.info("vote commit! " + player.getName() + " : " + sVote);
        final AVote banVote = getActiveVote();
        if (banVote == null) {
            BanVotePlugin.instance.msg(player, Language.INFO_NOVOTEACTIVE.toString());
            return;
        }
        BanVotePlugin.debug.info("vote activity check positive");

        if (sVote.equals("+") || sVote.equalsIgnoreCase("yes")
                || sVote.equalsIgnoreCase("true")) {
            BanVotePlugin.debug.info("committing " + banVote.getState().name()
                    + " vote: +" + banVote.getVoter() + " => "
                    + banVote.getTarget());

            banVote.commitYesVote(player);
            return;
        }

        if (sVote.equals("-") || sVote.equalsIgnoreCase("no")
                || sVote.equalsIgnoreCase("false")) {
            BanVotePlugin.debug.info("committing " + banVote.getState().name()
                    + " vote: -" + banVote.getVoter() + " => "
                    + banVote.getTarget());

            banVote.commitNoVote(player);
            return;
        }
        BanVotePlugin.debug.warn("vote value check fail");
        BanVotePlugin.instance.msg(player, Language.ERROR_INVALIDARGUMENT.toString(sVote));
        BanVotePlugin.instance.msg(player, Language.INFO_ARGUMENTS.toString(
                ChatColor.GREEN + "+"
                        + ChatColor.GOLD + "', '" + ChatColor.GREEN + "yes"
                        + ChatColor.GOLD + "', '" + ChatColor.GREEN + "true"
                        + ChatColor.GOLD + "', '" + ChatColor.RED + "-"
                        + ChatColor.GOLD + "', '" + ChatColor.RED + "no"
                        + ChatColor.GOLD + "', '" + ChatColor.RED + "false"
                        + ChatColor.GOLD));
    }

    /**
     * get the vote instance that currently is waiting for votes
     *
     * @return active class instance, null otherwise
     */
    public static AVote getActiveVote() {
        BanVotePlugin.debug.info("getting active vote");
        for (AVote banVote : BanVotePlugin.votes) {
            BanVotePlugin.debug.info("checking " + banVote.getState().name()
                    + " vote: -" + banVote.getVoter() + " => "
                    + banVote.getTarget());
            if (banVote.getState() == voteState.MUTETARGET
                    || banVote.getState() == voteState.MUTEVOTER) {
                return banVote;
            }
        }
        return null;
    }

    /**
     * check if a player is muted
     *
     * @param sPlayer the player name to check
     * @return true if the player is banned due to a running vote, false
     * otherwise
     */
    public static synchronized boolean isChatBlocked(final String sPlayer) {
        final Player player = Bukkit.getPlayer(sPlayer);
        if ((player != null) && (player.hasPermission("banvote.admin"))) {
            return false;
        }
        BanVotePlugin.debug.info("mute check: " + sPlayer);
        for (AVote banVote : BanVotePlugin.votes) {
            BanVotePlugin.debug.info("checking " + banVote.getState().name()
                    + " vote: " + banVote.getVoter() + " => "
                    + banVote.getTarget());
            if ((banVote.getTarget().equals(sPlayer) && banVote.getState() == voteState.MUTETARGET)
                    || (banVote.getVoter().equals(sPlayer) && banVote
                    .getState() == voteState.MUTEVOTER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * check if a vote is possible
     *
     * @param pTarget the player to check
     * @return false if a vote is running or vote on player is cooling down,
     * true otherwise
     */
    public static boolean isPossible(final Player pTarget) {
        BanVotePlugin.debug.info("vote check: " + pTarget.getName());
        if (getActiveVote() != null) {
            return false; // vote still active
        }
        BanVotePlugin.debug.info("vote check: " + pTarget.getName());
        for (AVote banVote : BanVotePlugin.votes) {
            BanVotePlugin.debug.info("checking " + banVote.getState().name()
                    + " vote: " + banVote.getVoter() + " => "
                    + banVote.getTarget());
            if (banVote.getTarget().equals(pTarget.getName())) {
                return false; // vote on target still cooling down
            }
        }
        return true; // no vote active
    }

    /**
     * parse ban vote type: byte to string
     *
     * @param bType the input byte
     * @return the output string
     */
    public static String parse(final byte bType) {
        if (bType == 0) {
            return "mute";
        } else if (bType == 1) {
            return "kick";
        } else if (bType == 2) {
            return "ban";
        }
        return BanVotePlugin.instance.getBVCommand(bType).getName();
    }

    /**
     * cancel the running task and start a new one based on a given interval
     *
     * @param interval the interval in ticks (20/s)
     */
    protected void restartRunnable(final int interval) {
        runner.cancel();
        BanVotePlugin.debug.info("restarting timer - interval: " + interval);
        runner = Bukkit
                .getServer()
                .getScheduler()
                .runTaskTimer(BanVotePlugin.instance,
                        new AdvanceRunner(this), interval, interval);
    }

    /**
     * remove a vote class instance
     *
     * @param banVote the instance to remove
     */
    protected static void remove(final AVote banVote) {
        BanVotePlugin.debug.info("removing " + banVote.getState().name() + " vote: -"
                + banVote.getVoter() + " => " + banVote.getTarget());
        BanVotePlugin.votes.remove(banVote);
    }

    public abstract void advance();

    public String getReason() {
        return reason;
    }
}
