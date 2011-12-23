package net.slipcor.banvote;

import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * ban vote manager class
 * 
 * @version v0.0.1
 * 
 * @author slipcor
 * 
 */

public class BanVoteManager {
	private static HashSet<BanVoteClass> votes = new HashSet<BanVoteClass>();

	/**
	 * check if a player is muted
	 * 
	 * @param sPlayer
	 *            the player name to check
	 * @return true if the player is banned due to a running vote, false
	 *         otherwise
	 */
	protected boolean isChatBlocked(String sPlayer) {
		BanVotePlugin.db.i("mute check: " + sPlayer);
		for (BanVoteClass banVote : votes) {
			BanVotePlugin.db.i("checking " + banVote.getState().name()
					+ " vote: " + banVote.getVoter() + " => "
					+ banVote.getTarget());
			if ((banVote.getTarget().equals(sPlayer) && banVote.getState() == BanVoteClass.voteState.MUTETARGET)
					|| (banVote.getVoter().equals(sPlayer) && banVote
							.getState() == BanVoteClass.voteState.MUTEVOTER)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * check if a vote is possible
	 * 
	 * @param pTarget
	 *            the player to check
	 * @return false if a vote is running or vote on player is cooling down,
	 *         true otherwise
	 */
	private boolean isPossible(Player pTarget) {
		BanVotePlugin.db.i("vote check: " + pTarget.getName());
		if (getActiveVote() != null) {
			return false; // vote still active
		}
		BanVotePlugin.db.i("vote check: " + pTarget.getName());
		for (BanVoteClass banVote : votes) {
			BanVotePlugin.db.i("checking " + banVote.getState().name()
					+ " vote: " + banVote.getVoter() + " => "
					+ banVote.getTarget());
			if (banVote.getTarget().equals(pTarget.getName())) {
				return false; // vote on target still cooling down
			}
		}
		return true; // no vote active
	}

	/**
	 * initiate a vote on a player
	 * 
	 * @param sTarget
	 *            the player name to possibly vote
	 * @param args
	 *            the command arguments
	 * @param player
	 *            the player trying to vote
	 */
	protected void init(String sTarget, String[] args, Player player) {
		BanVotePlugin.db.i("vote init: " + player.getName() + " => " + sTarget);
		BanVotePlugin.db.i("args: "
				+ BanVotePlugin.instance.parseStringArray(args));

		Player pTarget = null;

		try {
			pTarget = Bukkit.matchPlayer(sTarget).get(0);
			BanVotePlugin.db.i("player found: " + pTarget.getName());
		} catch (Exception e) {
			BanVotePlugin.db.w("player not found.");
		}
		if (pTarget == null) {
			BanVotePlugin.msg(player, "Player not found: " + sTarget);
			return;
		}
		if (!isPossible(pTarget)) {
			BanVotePlugin.msg(player, ChatColor.GOLD + "Vote on " + sTarget + " cooling down!");
			return;
		}
		BanVotePlugin.db.i("possibility check positive");
		votes.add(new BanVoteClass(pTarget, player, BanVotePlugin.instance
				.parseStringArray(args)));
	}

	/**
	 * try to commit a vote
	 * 
	 * @param sVote
	 *            vote value
	 * @param player
	 *            the player trying to vote
	 */
	protected void commit(String sVote, Player player) {
		BanVotePlugin.db.i("vote commit! " + player.getName() + " : " + sVote);
		BanVoteClass banVote = getActiveVote();
		if (banVote == null) {
			BanVotePlugin.msg(player, ChatColor.GOLD + "No vote active!");
			return;
		}
		BanVotePlugin.db.i("vote activity check positive");

		if (sVote.equals("+") || sVote.equalsIgnoreCase("yes")
				|| sVote.equalsIgnoreCase("true")) {
			BanVotePlugin.db.i("committing " + banVote.getState().name()
					+ " vote: +" + banVote.getVoter() + " => "
					+ banVote.getTarget());

			banVote.commitYesVote(player);
			return;
		}

		if (sVote.equals("-") || sVote.equalsIgnoreCase("no")
				|| sVote.equalsIgnoreCase("false")) {
			BanVotePlugin.db.i("committing " + banVote.getState().name()
					+ " vote: -" + banVote.getVoter() + " => "
					+ banVote.getTarget());

			banVote.commitNoVote(player);
			return;
		}
		BanVotePlugin.db.w("vote value check fail");
		BanVotePlugin.msg(player, ChatColor.GOLD + "Invalid vote argument '"
				+ sVote + "'!");
		BanVotePlugin.msg(player, ChatColor.GOLD
				+ "Use one of the following: '" + ChatColor.GREEN + "+"
				+ ChatColor.GOLD + "', '" + ChatColor.GREEN + "yes"
				+ ChatColor.GOLD + "', '" + ChatColor.GREEN + "true"
				+ ChatColor.GOLD + "', '" + ChatColor.RED + "-"
				+ ChatColor.GOLD + "', '" + ChatColor.RED + "no"
				+ ChatColor.GOLD + "', '" + ChatColor.RED + "false"
				+ ChatColor.GOLD + "'!");
	}

	/**
	 * remove a vote class instance
	 * 
	 * @param banVote
	 *            the instance to remove
	 */
	protected static void remove(BanVoteClass banVote) {
		BanVotePlugin.db.i("removing " + banVote.getState().name() + " vote: -"
				+ banVote.getVoter() + " => " + banVote.getTarget());
		votes.remove(banVote);
	}

	/**
	 * get the vote instance that currently is waiting for votes
	 * 
	 * @return active class instance, null otherwise
	 */
	private BanVoteClass getActiveVote() {
		BanVotePlugin.db.i("getting active vote");
		for (BanVoteClass banVote : votes) {
			BanVotePlugin.db.i("checking " + banVote.getState().name()
					+ " vote: -" + banVote.getVoter() + " => "
					+ banVote.getTarget());
			if (banVote.getState() == BanVoteClass.voteState.MUTETARGET
					|| banVote.getState() == BanVoteClass.voteState.MUTEVOTER) {
				return banVote;
			}
		}
		return null;
	}
}
