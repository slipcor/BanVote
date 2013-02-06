package net.slipcor.banvote.votes;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.slipcor.banvote.BanVotePlugin;
import net.slipcor.banvote.api.AVote;
import net.slipcor.banvote.util.Config;

public class GeneralVote extends AVote {
	public GeneralVote(Player pTarget, Player player, String sReason,
			byte bType) {
		super(pTarget, player, sReason, bType);

		BanVotePlugin.instance.brc(ChatColor.GREEN + player.getName() + ChatColor.GOLD
				+ " started a " + type + " vote.");
		BanVotePlugin.instance.brc(ChatColor.GOLD + type + " reason: " + ChatColor.WHITE
				+ sReason);
		BanVotePlugin.instance.brc(ChatColor.GOLD + "Say " + ChatColor.GREEN + "/"
				+ (bType > 2 ? "custom" : type) + "vote yes" + ChatColor.GOLD
				+ " for " + type + ", " + ChatColor.RED + "/"
				+ (bType > 2 ? "custom" : type) + "vote no" + ChatColor.GOLD
				+ " to vote against " + type + ".");
		BanVotePlugin.log.info(type + " vote started: [voter: "
				+ player.getName() + "], reason: " + sReason);
	}

	@Override
	public void advance() {
		
		//TODO debug what happens here, whats the state, whats half
		// mutetarget, half = false
		
		if (state == voteState.MUTETARGET) {
			if (half) {
				calculateResult();
			} else {
				BanVotePlugin.instance.brc(ChatColor.GOLD
						+ String.valueOf(Math.round(Config.stageSeconds / 2))
						+ " seconds until vote is over.");
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
			// vote successful
			BanVotePlugin.instance.brc(ChatColor.GOLD + "" + type + " vote"
					+ " gave a clear result.");
			BanVotePlugin.instance.brc(ChatColor.GOLD + "It " + ChatColor.GREEN
					+ "succeeded" + ChatColor.GOLD + " with a score of "
					+ Math.round(result) + ".");

			state = voteState.POSITIVE;
			
			byte action = BanVotePlugin.instance.getBVCommand(type).getAction();
			
			String cmd = BanVotePlugin.instance.getBVCommand(action).getCommand();

			cmd = commandReplace(cmd, "", 0);

			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
			return;
			
		} else if (result < Config.validMax) {
			// vote failed
			BanVotePlugin.instance.brc(ChatColor.GOLD + "" + type + " vote"
					+ " gave a clear result.");
			BanVotePlugin.instance.brc(ChatColor.GOLD + "It " + ChatColor.RED + "failed"
					+ ChatColor.GOLD + " with a score of " + Math.round(result)
					+ ".");

			state = voteState.NEGATIVE;
		} else {
			// community failed
			BanVotePlugin.instance.brc(ChatColor.GOLD + "" + type + " vote" +
					" did not give a clear result.");

			state = voteState.NULL;
		}

		final int interval = 20 * 60 * Config.coolMinutes; // minutes
		restartRunnable(interval);
		// "the total is shown"
	}
}
