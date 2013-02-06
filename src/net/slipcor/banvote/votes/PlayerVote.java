package net.slipcor.banvote.votes;

import java.util.Set;

import net.slipcor.banvote.BanVotePlugin;
import net.slipcor.banvote.BanVoteResult;
import net.slipcor.banvote.api.AVote;
import net.slipcor.banvote.util.Config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * ban vote class
 * 
 * @version v0.0.6
 * 
 * @author slipcor
 * 
 */

public class PlayerVote extends AVote {

	/**
	 * Creates a vote class instance, announces the vote and starts its timer
	 * 
	 * @param pTarget
	 *            the player that is subject of a possible ban
	 * @param player
	 *            the player that initiated the ban
	 * @param sReason
	 *            the reason given for banning
	 */
	public PlayerVote(final Player pTarget, final Player player, final String sReason, final byte bType) {
		super(pTarget, player, sReason, bType);

		BanVotePlugin.instance.brc(ChatColor.GREEN + player.getName() + ChatColor.GOLD
				+ " started a " + type + " vote against " + ChatColor.RED
				+ pTarget==null?"null":pTarget.getName() + ChatColor.GOLD + ".");
		BanVotePlugin.instance.brc(ChatColor.GOLD + type + " reason: " + ChatColor.WHITE
				+ sReason);
		BanVotePlugin.instance.brc(ChatColor.GOLD + "Say " + ChatColor.GREEN + "/"
				+ (bType > 2 ? "custom" : type) + "vote yes" + ChatColor.GOLD
				+ " for " + type + ", " + ChatColor.RED + "/"
				+ (bType > 2 ? "custom" : type) + "vote no" + ChatColor.GOLD
				+ " to vote against " + type + ".");
		BanVotePlugin.instance.brc(ChatColor.GOLD + "Muting " + ChatColor.RED
				+ pTarget==null?"null":pTarget.getName() + ChatColor.GOLD + " for " + Config.stageSeconds
				+ " seconds to discuss the " + type + " vote.");
		BanVotePlugin.log.info(type + " vote started: [voter: "
				+ player.getName() + "], [target: " + pTarget==null?"null":pTarget.getName()
				+ "], reason: " + sReason);
	}

	/**
	 * advance the vote ban to the next stage
	 */
	@Override
	public void advance() {
		if (state == voteState.MUTETARGET) {
			if (half) {
				state = voteState.MUTEVOTER;
				BanVotePlugin.instance.brc(ChatColor.GOLD + "Muting " + ChatColor.GREEN
						+ voter + ChatColor.GOLD + " for " + Config.stageSeconds
						+ " seconds, so " + ChatColor.RED + target
						+ ChatColor.GOLD + " can explain.");
				BanVotePlugin.log.info(type
						+ " vote: stage 2 - muting the voter");
			} else {
				BanVotePlugin.instance.brc(ChatColor.GOLD
						+ String.valueOf(Math.round(Config.stageSeconds / 2))
						+ " seconds until " + ChatColor.RED + target
						+ ChatColor.GOLD + " is unmuted.");
			}
			half = !half;
		} else if (state == voteState.MUTEVOTER) {
			if (half) {
				calculateResult();
			} else {
				BanVotePlugin.instance.brc(ChatColor.GOLD
						+ String.valueOf(Math.round(Config.stageSeconds / 2))
						+ " seconds until " + ChatColor.GREEN + voter
						+ ChatColor.GOLD + " is unmuted.");
			}
			half = !half;
		} else {
			// cooldown finished, remove!
			runner.cancel();
			runner = null;
			PlayerVote.remove(this);
		}
	}

	/**
	 * calculate the vote result and commit bans, including timers
	 */
	private void calculateResult() {
		BanVotePlugin.debug.info("calculating vote result");
		int iAfk = 0;

		iAfk = getAfk().size();

		final Set<String> afk = getAfk();
		final Set<String> non = getNon(afk);

		final float result = (Config.yesValue * yes.size()) + (Config.noValue * nope.size())
				+ (Config.afkValue * iAfk) + (Config.nonValue * non.size());

		BanVotePlugin.instance.brc(ChatColor.GOLD + "Voters for " + ChatColor.GREEN
				+ type + ChatColor.GOLD + ": " + getNames(yes));
		BanVotePlugin.instance.brc(ChatColor.GOLD + "Voters for " + ChatColor.RED
				+ "no " + type + ChatColor.GOLD + ": " + getNames(nope));

		if (Config.calcPublic) {

			BanVotePlugin.instance.brc(yes.size() + " " + type + " votes = "
					+ (yes.size() * Config.yesValue) + " :: " + getNames(yes));
			BanVotePlugin.instance.brc(afk.size() + " afk votes = "
					+ (afk.size() * Config.afkValue) + " :: " + getNames(afk));
			BanVotePlugin.instance.brc(nope.size() + " anti votes = "
					+ (nope.size() * Config.noValue) + " :: " + getNames(nope));
			BanVotePlugin.instance.brc(non.size() + " non votes = "
					+ (non.size() * Config.nonValue) + " :: " + getNames(non));
			BanVotePlugin.instance.brc("------------------");
			BanVotePlugin.instance.brc("Final vote tally = " + result);
		} else {
			BanVotePlugin.log.info(yes.size() + " " + type + " votes = "
					+ (yes.size() * Config.yesValue) + " :: " + getNames(yes));
			BanVotePlugin.log.info(afk.size() + " afk votes = "
					+ (afk.size() * Config.afkValue) + " :: " + getNames(afk));
			BanVotePlugin.log.info(nope.size() + " anti votes = "
					+ (nope.size() * Config.noValue) + " :: " + getNames(nope));
			BanVotePlugin.log.info(non.size() + " non votes = "
					+ (non.size() * Config.nonValue) + " :: " + getNames(non));
			BanVotePlugin.log.info("------------------");
			BanVotePlugin.log.info("Final vote tally = " + result);
		}

		if (result > Config.validMin) {
			// ban successful
			BanVotePlugin.instance.brc(ChatColor.GOLD + "" + type + " vote on "
					+ ChatColor.RED + target + ChatColor.GOLD
					+ " gave a clear result.");
			BanVotePlugin.instance.brc(ChatColor.GOLD + "It " + ChatColor.GREEN
					+ "succeeded" + ChatColor.GOLD + " with a score of "
					+ Math.round(result) + ".");
			if (type.equals("ban")) {
				BanVotePlugin.instance.brc(ChatColor.GOLD + "Banning " + ChatColor.RED
						+ target + ChatColor.GOLD + ".");
			}

			state = voteState.POSITIVE;
			BanVotePlugin.log.info(target + " tempban = " + result * Config.posMinutes);
			commitBan(target, Math.round(result * Config.posMinutes));

		} else if (result < Config.validMax) {
			// ban failed
			BanVotePlugin.instance.brc(ChatColor.GOLD + "" + type + " vote on "
					+ ChatColor.RED + target + ChatColor.GOLD
					+ " gave a clear result.");
			BanVotePlugin.instance.brc(ChatColor.GOLD + "It " + ChatColor.RED + "failed"
					+ ChatColor.GOLD + " with a score of " + Math.round(result)
					+ ".");
			if (type.equals("ban")) {
				BanVotePlugin.instance.brc(ChatColor.GOLD + "Banning " + ChatColor.GREEN
						+ voter + ChatColor.GOLD + ".");
			}

			state = voteState.NEGATIVE;
			BanVotePlugin.log.info(voter + " tempban = " + result * Config.negMinutes);
			commitBan(voter, Math.round(result * Config.negMinutes));
		} else {
			// community failed
			BanVotePlugin.instance.brc(ChatColor.GOLD + "" + type + " vote on "
					+ ChatColor.RED + target + ChatColor.GOLD
					+ " did not give a clear result.");

			state = voteState.NULL;
		}

		final int interval = 20 * 60 * Config.coolMinutes; // minutes
		restartRunnable(interval);
		// "the total is shown"
	}

	/**
	 * actually commit the ban/mute/kick command,
	 * TODO calculate ban/mute count
	 * to maybe perm ban
	 * 
	 * @param target
	 *            playername to be banned
	 * @param i
	 *            value in minutes to be banned
	 */
	private void commitBan(final String sBanTarget, final int mins) {
		int iMins = mins;
		byte action = -1;
		if (type.equals("mute")) {
			action = 0;
		} else if (type.equals("kick")) {
			action = 1;
		} else if (type.equals("ban")) {
			action = 2;
		} else {
			action = BanVotePlugin.instance.getBVCommand(type).getAction();
			if (BanVotePlugin.instance.getBVCommand(type).doesBan()
					|| BanVotePlugin.instance.getBVCommand(type).doesKick()) {
				try {
					Bukkit.getPlayer(sBanTarget).kickPlayer(
							"You have been vote-banned for " + iMins + " minutes!");
				} catch (Exception e) {
					// mooh
				}
			}
			String cmd = BanVotePlugin.instance.getBVCommand(action).getCommand();

			cmd = commandReplace(cmd, sBanTarget, iMins);

			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
			return;
		}
		iMins = Math.abs(iMins);

		if (action < 0) {
			return;
		}

		BanVoteResult.add(voter + ":" + target + ":"
				+ Math.round(System.currentTimeMillis() / 1000) + ":" + iMins + ":"
				+ target.equals(sBanTarget) + ":" + action);
		BanVotePlugin.debug.info("committing " + type + " on " + target + " for " + iMins
				+ " minutes");
		if (action == 0) {
			BanVotePlugin.instance.brc("Muting " + sBanTarget + " for " + iMins + " minutes");
			BanVotePlugin.debug.info("NOT kicking");
			return;
		}
		if (action == 1 || action == 2) {
			try {
				Bukkit.getPlayer(sBanTarget).kickPlayer(
						"You have been vote-banned for " + iMins + " minutes!");
			} catch (Exception e) {
				// mooh
			}
			return;
		}

	}
}
