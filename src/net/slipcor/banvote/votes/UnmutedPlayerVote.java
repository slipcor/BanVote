package net.slipcor.banvote.votes;

import java.util.Set;

import net.slipcor.banvote.BanVotePlugin;
import net.slipcor.banvote.BanVoteResult;
import net.slipcor.banvote.api.AVote;
import net.slipcor.banvote.util.Config;
import net.slipcor.banvote.util.Language;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * ban vote class
 * 
 * @author slipcor
 * 
 */

public class UnmutedPlayerVote extends AVote {

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
	public UnmutedPlayerVote(final Player pTarget, final Player player, final String sReason, final byte bType) {
		super(pTarget, player, sReason, bType);

		BanVotePlugin.instance.brc(Language.INFO_PLAYER_INIT1.toString(
				player.getName(),type,pTarget==null?"null":pTarget.getName()));
		BanVotePlugin.instance.brc(Language.INFO_PLAYER_INIT2.toString(
				type,sReason));
		BanVotePlugin.instance.brc(Language.INFO_PLAYER_INIT3.toString(
				(bType > 2 ? "custom" : type),type,(bType > 2 ? "custom" : type),type));
		
		BanVotePlugin.instance.getLogger().info(Language.LOG_STARTEDTARGET.toString(
				type,player.getName(),pTarget==null?"null":pTarget.getName(),sReason));
	}

	/**
	 * advance the vote ban to the next stage
	 */
	@Override
	public void advance() {
		if (state == voteState.MUTETARGET) {
			if (half) {
				calculateResult();
			} else {
				BanVotePlugin.instance.brc(Language.INFO_GENERALSECONDS.toString(
						String.valueOf(Math.round(Config.stageSeconds / 2))));
			}
			half = !half;
		} else {
			// cooldown finished, remove!
			runner.cancel();
			runner = null;
			UnmutedPlayerVote.remove(this);
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

		BanVotePlugin.instance.brc(Language.INFO_RESULTYES.toString(type,getNames(yes)));
		BanVotePlugin.instance.brc(Language.INFO_RESULTNO.toString(type,getNames(nope)));

		if (Config.calcPublic) {
			BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARY1.toString(
					String.valueOf(yes.size()),type,String.valueOf(yes.size() * Config.yesValue),getNames(yes)));
			BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARY2.toString(
					String.valueOf(afk.size()),type,String.valueOf(afk.size() * Config.afkValue),getNames(afk)));
			BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARY3.toString(
					String.valueOf(nope.size()),type,String.valueOf(nope.size() * Config.yesValue),getNames(nope)));
			BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARY4.toString(
					String.valueOf(non.size()),type,String.valueOf(non.size() * Config.yesValue),getNames(non)));
			BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARYLINE.toString());
			BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARYRESULT.toString(String.valueOf(result)));
		} else {
			BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARY1.toString(
					String.valueOf(yes.size()),type,String.valueOf(yes.size() * Config.yesValue),getNames(yes)));
			BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARY2.toString(
					String.valueOf(afk.size()),type,String.valueOf(afk.size() * Config.afkValue),getNames(afk)));
			BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARY3.toString(
					String.valueOf(nope.size()),type,String.valueOf(nope.size() * Config.yesValue),getNames(nope)));
			BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARY4.toString(
					String.valueOf(non.size()),type,String.valueOf(non.size() * Config.yesValue),getNames(non)));
			BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARYLINE.toString());
			BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARYRESULT.toString(String.valueOf(result)));
		}

		if (result > Config.validMin) {
			// ban successful
			BanVotePlugin.instance.brc(Language.INFO_PLAYERVOTERESULTCLEAR.toString(type,target));
			BanVotePlugin.instance.brc(Language.INFO_VOTERESULTYES.toString(String.valueOf(Math.round(result))));

			if (type.equals("ban")) {
				BanVotePlugin.instance.brc(Language.INFO_BANNING.toString(target));
			}

			state = voteState.POSITIVE;
			BanVotePlugin.instance.getLogger().info(target + " tempban = " + result * Config.posMinutes);
			commitBan(target, Math.round(result * Config.posMinutes));

		} else if (result < Config.validMax) {
			// ban failed
			BanVotePlugin.instance.brc(Language.INFO_PLAYERVOTERESULTCLEAR.toString(type,target));
			BanVotePlugin.instance.brc(Language.INFO_VOTERESULTNO.toString(String.valueOf(Math.round(result))));
			
			if (type.equals("ban")) {
				BanVotePlugin.instance.brc(Language.INFO_BANNINGVOTER.toString(voter));
			}

			state = voteState.NEGATIVE;
			BanVotePlugin.instance.getLogger().info(voter + " tempban = " + result * Config.negMinutes);
			commitBan(voter, Math.round(result * Config.negMinutes));
		} else {
			// community failed
			BanVotePlugin.instance.brc(Language.INFO_PLAYERVOTERESULTNOTCLEAR.toString(type,target));

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
							Language.INFO_VOTEBANNEDSECONDS.toString(String.valueOf(iMins)));
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
			BanVotePlugin.instance.brc(Language.INFO_MUTINGSECONDS.toString(
					sBanTarget,String.valueOf(iMins*60)));
			BanVotePlugin.debug.info("NOT kicking");
			return;
		}
		if (action == 1 || action == 2) {
			try {
				Bukkit.getPlayer(sBanTarget).kickPlayer(
						Language.INFO_VOTEBANNEDSECONDS.toString(String.valueOf(iMins)));
			} catch (Exception e) {
				// mooh
			}
			return;
		}

	}
}
