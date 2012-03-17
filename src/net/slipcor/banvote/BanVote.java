package net.slipcor.banvote;

import java.util.HashSet;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.stonedCRAFT.SimpleAFK.SimpleAFK;

/**
 * ban vote class
 * 
 * @version v0.0.4
 * 
 * @author slipcor
 * 
 */

public class BanVote {
	protected static enum voteState {
		MUTETARGET, MUTEVOTER, POSITIVE, NEGATIVE, NULL
	}

	private static int stageSeconds;
	private static float noValue;
	private static float yesValue;
	private static float afkValue;
	private static float nonValue;
	private static float validMin;
	private static float validMax;
	private static int posMinutes;
	private static int negMinutes;
	private static int coolMinutes;
	private static boolean calcPublic;

	private String type;
	private voteState state;
	private String voter;
	private String target;
	private int RUN_ID;
	private boolean half = false;
	private String reason;

	private HashSet<String> yes = new HashSet<String>();
	private HashSet<String> no = new HashSet<String>();

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
	public BanVote(Player pTarget, Player player, String sReason, byte bType) {
		voter = player.getName();
		target = pTarget.getName();
		state = voteState.MUTETARGET;

		type = parse(bType);

		BanVotePlugin.brc(ChatColor.GREEN + player.getName() + ChatColor.GOLD
				+ " started a " + type + " vote against " + ChatColor.RED
				+ pTarget.getName() + ChatColor.GOLD + ".");
		BanVotePlugin.brc(ChatColor.GOLD + type + " reason: " + ChatColor.WHITE
				+ sReason);
		BanVotePlugin.brc(ChatColor.GOLD + "Say " + ChatColor.GREEN + "/"
				+ (bType > 2 ? "custom" : type) + "vote yes" + ChatColor.GOLD
				+ " for " + type + ", " + ChatColor.RED + "/"
				+ (bType > 2 ? "custom" : type) + "vote no" + ChatColor.GOLD
				+ " to vote against " + type + ".");
		BanVotePlugin.brc(ChatColor.GOLD + "Muting " + ChatColor.RED
				+ pTarget.getName() + ChatColor.GOLD + " for " + stageSeconds
				+ " seconds to discuss the " + type + " vote.");
		BanVotePlugin.log.i("" + type + " vote started: [voter: "
				+ player.getName() + "], [target: " + pTarget.getName()
				+ "], reason: " + sReason);
		int interval = 20 * Math.round(stageSeconds / 2); // half a minute
		BanVotePlugin.db.i("" + type + "Vote interval: " + interval + " ticks");
		
		reason = sReason;

		RUN_ID = Bukkit
				.getServer()
				.getScheduler()
				.scheduleSyncRepeatingTask(BanVotePlugin.instance,
						new BVRunnable(this), interval, interval);
	}

	/**
	 * check if a player is muted
	 * 
	 * @param sPlayer
	 *            the player name to check
	 * @return true if the player is banned due to a running vote, false
	 *         otherwise
	 */
	protected static boolean isChatBlocked(String sPlayer) {
		Player p = Bukkit.getPlayer(sPlayer);
		if (p != null) {
			if (p.hasPermission("banvote.admin")) {
				return false;
			}
		}
		BanVotePlugin.db.i("mute check: " + sPlayer);
		for (BanVote banVote : BanVotePlugin.votes) {
			BanVotePlugin.db.i("checking " + banVote.getState().name()
					+ " vote: " + banVote.getVoter() + " => "
					+ banVote.getTarget());
			if ((banVote.getTarget().equals(sPlayer) && banVote.getState() == BanVote.voteState.MUTETARGET)
					|| (banVote.getVoter().equals(sPlayer) && banVote
							.getState() == BanVote.voteState.MUTEVOTER)) {
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
	private static boolean isPossible(Player pTarget) {
		BanVotePlugin.db.i("vote check: " + pTarget.getName());
		if (getActiveVote() != null) {
			return false; // vote still active
		}
		BanVotePlugin.db.i("vote check: " + pTarget.getName());
		for (BanVote banVote : BanVotePlugin.votes) {
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
	protected static void init(String sTarget, String[] args, Player player,
			byte b) {
		BanVotePlugin.db.i("vote init: " + player.getName() + " => " + sTarget);
		BanVotePlugin.db.i("args: "
				+ BanVotePlugin.instance.parseStringArray(args, b));

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
			BanVotePlugin.msg(player, ChatColor.GOLD + "Vote on " + sTarget
					+ " cooling down!");
			return;
		}
		BanVotePlugin.db.i("possibility check positive");
		BanVotePlugin.votes.add(new BanVote(pTarget, player,
				BanVotePlugin.instance.parseStringArray(args, b), b));
	}

	/**
	 * try to commit a vote
	 * 
	 * @param sVote
	 *            vote value
	 * @param player
	 *            the player trying to vote
	 */
	protected static void commit(String sVote, Player player) {
		BanVotePlugin.db.i("vote commit! " + player.getName() + " : " + sVote);
		BanVote banVote = getActiveVote();
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
	protected static void remove(BanVote banVote) {
		BanVotePlugin.db.i("removing " + banVote.getState().name() + " vote: -"
				+ banVote.getVoter() + " => " + banVote.getTarget());
		BanVotePlugin.votes.remove(banVote);
	}

	/**
	 * get the vote instance that currently is waiting for votes
	 * 
	 * @return active class instance, null otherwise
	 */
	private static BanVote getActiveVote() {
		BanVotePlugin.db.i("getting active vote");
		for (BanVote banVote : BanVotePlugin.votes) {
			BanVotePlugin.db.i("checking " + banVote.getState().name()
					+ " vote: -" + banVote.getVoter() + " => "
					+ banVote.getTarget());
			if (banVote.getState() == BanVote.voteState.MUTETARGET
					|| banVote.getState() == BanVote.voteState.MUTEVOTER) {
				return banVote;
			}
		}
		return null;
	}

	/**
	 * parse ban vote type: byte to string
	 * 
	 * @param bType
	 *            the input byte
	 * @return the output string
	 */
	protected static String parse(byte bType) {
		if (bType == 0) {
			return "mute";
		} else if (bType == 1) {
			return "kick";
		} else if (bType == 2) {
			return "ban";
		}
		return BanVotePlugin.instance.getCommandName(bType);
	}

	/**
	 * 
	 * parse ban vote type: byte to string
	 * 
	 * @param bType
	 *            the input byte
	 * 
	 * @return the output string
	 */

	protected static byte parse(String sType) {

		if (sType.equals("kick")) {
			return 1;
		} else if (sType.equals("ban")) {
			return 2;
		}
		return 0;
	}

	/**
	 * hand over vote state
	 * 
	 * @return state enum
	 */
	protected voteState getState() {
		return state;
	}

	/**
	 * hand over the target
	 * 
	 * @return target player name
	 */
	protected String getTarget() {
		return target;
	}

	/**
	 * hand over voting player name
	 * 
	 * @return voting player name
	 */
	protected String getVoter() {
		return voter;
	}

	/**
	 * calculate afk players
	 * 
	 * @return hashset of all afk players
	 */
	private HashSet<String> getAfk() {
		HashSet<String> afk = new HashSet<String>();

		try {
			if (BanVotePlugin.instance.getServer().getPluginManager()
					.getPlugin("SimpleAFK") == null) {
				return afk;
			}

			SimpleAFK plugin = (SimpleAFK) BanVotePlugin.instance.getServer()
					.getPluginManager().getPlugin("SimpleAFK");

			for (Player p : plugin.afkPlayers.keySet()) {
				if (yes.contains(p.getName())) {
					continue;
				}
				if (no.contains(p.getName())) {
					continue;
				}
				afk.add(p.getName());
			}
		} catch (Exception E) {

		}

		return afk;
	}

	/**
	 * return a string containing all content of a given hashset joined with a
	 * space
	 * 
	 * @param set
	 *            the hashset to join
	 * @return a string with all entries
	 */
	private String getNames(HashSet<String> set) {
		String result = "";
		for (String s : set) {
			result += (!result.equals("")) ? (", " + s) : s;
		}
		return result;
	}

	/**
	 * construct a hashset based on players not in any other hashset
	 * 
	 * @param afk
	 *            the afk hashset
	 * @return the hashset of players not voted and not afk
	 */
	private HashSet<String> getNon(HashSet<String> afk) {
		HashSet<String> non = new HashSet<String>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (yes.contains(p.getName())) {
				continue;
			}
			if (no.contains(p.getName())) {
				continue;
			}
			if (afk.contains(p.getName())) {
				continue;
			}
			BanVotePlugin.db.i("getNon - adding: " + p.getName());
			non.add(p.getName());
		}
		return non;
	}

	/**
	 * advance the vote ban to the next stage
	 */
	protected void advance() {
		if (state == voteState.MUTETARGET) {
			if (half) {
				state = voteState.MUTEVOTER;
				BanVotePlugin.brc(ChatColor.GOLD + "Muting " + ChatColor.GREEN
						+ voter + ChatColor.GOLD + " for " + stageSeconds
						+ " seconds, so " + ChatColor.RED + target
						+ ChatColor.GOLD + " can explain.");
				BanVotePlugin.log.i("" + type
						+ " vote: stage 2 - muting the voter");
			} else {
				BanVotePlugin.brc(ChatColor.GOLD
						+ String.valueOf(Math.round(stageSeconds / 2))
						+ " seconds until " + ChatColor.RED + target
						+ ChatColor.GOLD + " is unmuted.");
			}
			half = !half;
		} else if (state == voteState.MUTEVOTER) {
			if (half) {
				calculateResult();
			} else {
				BanVotePlugin.brc(ChatColor.GOLD
						+ String.valueOf(Math.round(stageSeconds / 2))
						+ " seconds until " + ChatColor.GREEN + voter
						+ ChatColor.GOLD + " is unmuted.");
			}
			half = !half;
		} else {
			// cooldown finished, remove!
			Bukkit.getScheduler().cancelTask(RUN_ID);
			BanVote.remove(this);
		}
	}

	/**
	 * calculate the vote result and commit bans, including timers
	 */
	private void calculateResult() {
		BanVotePlugin.db.i("calculating vote result");
		int iAfk = 0;

		iAfk = getAfk().size();

		HashSet<String> afk = new HashSet<String>();
		HashSet<String> non = new HashSet<String>();

		afk = getAfk();
		non = getNon(afk);

		float result = (yesValue * yes.size()) + (noValue * no.size())
				+ (afkValue * iAfk) + (nonValue * non.size());

		BanVotePlugin.brc(ChatColor.GOLD + "Voters for " + ChatColor.GREEN
				+ type + ChatColor.GOLD + ": " + getNames(yes));
		BanVotePlugin.brc(ChatColor.GOLD + "Voters for " + ChatColor.RED
				+ "no " + type + ChatColor.GOLD + ": " + getNames(no));

		if (calcPublic) {

			BanVotePlugin.brc(yes.size() + " " + type + " votes = "
					+ (yes.size() * yesValue) + " :: " + getNames(yes));
			BanVotePlugin.brc(afk.size() + " afk votes = "
					+ (afk.size() * afkValue) + " :: " + getNames(afk));
			BanVotePlugin.brc(no.size() + " anti votes = "
					+ (no.size() * noValue) + " :: " + getNames(no));
			BanVotePlugin.brc(non.size() + " non votes = "
					+ (non.size() * nonValue) + " :: " + getNames(non));
			BanVotePlugin.brc("------------------");
			BanVotePlugin.brc("Final vote tally = " + result);
		} else {
			BanVotePlugin.log.i(yes.size() + " " + type + " votes = "
					+ (yes.size() * yesValue) + " :: " + getNames(yes));
			BanVotePlugin.log.i(afk.size() + " afk votes = "
					+ (afk.size() * afkValue) + " :: " + getNames(afk));
			BanVotePlugin.log.i(no.size() + " anti votes = "
					+ (no.size() * noValue) + " :: " + getNames(no));
			BanVotePlugin.log.i(non.size() + " non votes = "
					+ (non.size() * nonValue) + " :: " + getNames(non));
			BanVotePlugin.log.i("------------------");
			BanVotePlugin.log.i("Final vote tally = " + result);
		}

		if (result > validMin) {
			// ban successful
			BanVotePlugin.brc(ChatColor.GOLD + "" + type + " vote on "
					+ ChatColor.RED + target + ChatColor.GOLD
					+ " gave a clear result.");
			BanVotePlugin.brc(ChatColor.GOLD + "It " + ChatColor.GREEN
					+ "succeeded" + ChatColor.GOLD + " with a score of "
					+ Math.round(result) + ".");
			if (type.equals("ban")) {
				BanVotePlugin.brc(ChatColor.GOLD + "Banning " + ChatColor.RED
						+ target + ChatColor.GOLD + ".");
			}

			state = voteState.POSITIVE;
			BanVotePlugin.log.i(target + " tempban = " + result * posMinutes);
			commitBan(target, Math.round(result * posMinutes));

		} else if (result < validMax) {
			// ban failed
			BanVotePlugin.brc(ChatColor.GOLD + "" + type + " vote on "
					+ ChatColor.RED + target + ChatColor.GOLD
					+ " gave a clear result.");
			BanVotePlugin.brc(ChatColor.GOLD + "It " + ChatColor.RED + "failed"
					+ ChatColor.GOLD + " with a score of " + Math.round(result)
					+ ".");
			if (type.equals("ban")) {
				BanVotePlugin.brc(ChatColor.GOLD + "Banning " + ChatColor.GREEN
						+ voter + ChatColor.GOLD + ".");
			}

			state = voteState.NEGATIVE;
			BanVotePlugin.log.i(voter + " tempban = " + result * negMinutes);
			commitBan(voter, Math.round(result * negMinutes));
		} else {
			// community failed
			BanVotePlugin.brc(ChatColor.GOLD + "" + type + " vote on "
					+ ChatColor.RED + target + ChatColor.GOLD
					+ " did not give a clear result.");

			state = voteState.NULL;
		}

		int interval = 20 * 60 * coolMinutes; // minutes
		restartRunnable(interval);
		// "the total is shown"
	}

	/**
	 * actually commit the ban/mute/kick command, TODO calculate ban/mute count
	 * to maybe perm ban
	 * 
	 * @param target
	 *            playername to be banned
	 * @param i
	 *            value in minutes to be banned
	 */
	private void commitBan(String sBanTarget, int i) {
		byte b = -1;
		if (type.equals("mute")) {
			b = 0;
		} else if (type.equals("kick")) {
			b = 1;
		} else if (type.equals("ban")) {
			b = 2;
		} else {
			b = BanVotePlugin.instance.getCommandNumber(type);
			if (BanVotePlugin.commands.get(type).doesBan()
					|| BanVotePlugin.commands.get(type).doesKick()) {
				try {
					Bukkit.getPlayer(sBanTarget).kickPlayer(
							"You have been vote-banned for " + i + " minutes!");
				} catch (Exception e) {
					// mooh
				}
			}
			String cmd = BanVotePlugin.instance.getCommand(b);

			cmd = commandReplace(cmd, sBanTarget, i);

			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
			return;
		}
		i = Math.abs(i);

		if (b < 0) {
			return;
		}

		BanVoteResult.add(voter + ":" + target + ":"
				+ Math.round(System.currentTimeMillis() / 1000) + ":" + i + ":"
				+ target.equals(sBanTarget) + ":" + b);
		BanVotePlugin.db.i("committing " + type + " on " + target + " for " + i
				+ " minutes");
		if (b == 0) {
			BanVotePlugin
					.brc("Muting " + sBanTarget + " for " + i + " minutes");
			BanVotePlugin.db.i("NOT kicking");
			return;
		}
		if (b == 1 || b == 2) {
			try {
				Bukkit.getPlayer(sBanTarget).kickPlayer(
						"You have been vote-banned for " + i + " minutes!");
			} catch (Exception e) {
				// mooh
			}
			return;
		}

	}

	private String commandReplace(String cmd, String sBanTarget, int minutes) {
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

		cmd = cmd.replace("$w", (voter.equals(sBanTarget) ? target : voter));
		cmd = cmd.replace("$l", sBanTarget);

		cmd = cmd.replace("$m", String.valueOf(minutes));
		cmd = cmd.replace("$h", String.valueOf(minutes / 60));
		cmd = cmd.replace("$s", String.valueOf(minutes * 60));

		cmd = cmd.replace("$fm", String.valueOf(minutes % 60));
		cmd = cmd.replace("$fh", String.valueOf(Math.floor(minutes / 60)));
		reason = reason == "null" ? "" : reason;
		cmd = cmd.replace("$r", reason);

		return cmd;
	}

	/**
	 * commit a negative vote
	 * 
	 * @param player
	 *            the voting player
	 */
	protected void commitNoVote(Player player) {
		BanVotePlugin.db.i("player " + player.getName() + " votes NO");
		if (!mayVote(player.getName())) {
			BanVotePlugin.msg(player, ChatColor.GOLD + "You already voted!");
			return;
		}
		no.add(player.getName());
		BanVotePlugin.msg(player, ChatColor.RED + "Vote successful!");
	}

	/**
	 * commit a positive vote
	 * 
	 * @param player
	 *            the voting player
	 */
	protected void commitYesVote(Player player) {
		BanVotePlugin.db.i("player " + player.getName() + " votes YES");
		if (!mayVote(player.getName())) {
			BanVotePlugin.msg(player, ChatColor.GOLD + "You already voted!");
			return;
		}
		yes.add(player.getName());
		BanVotePlugin.msg(player, ChatColor.GREEN + "Vote successful!");
	}

	/**
	 * cancel the running task and start a new one based on a given interval
	 * 
	 * @param interval
	 *            the interval in ticks (20/s)
	 */
	private void restartRunnable(int interval) {
		Bukkit.getScheduler().cancelTask(RUN_ID);
		BanVotePlugin.db.i("restarting timer - interval: " + interval);
		RUN_ID = Bukkit
				.getServer()
				.getScheduler()
				.scheduleSyncRepeatingTask(BanVotePlugin.instance,
						new BVRunnable(this), interval, interval);
	}

	/**
	 * check if a player may vote
	 * 
	 * @param sPlayer
	 *            the player name to check
	 * @return true if a player may vote, false otherwise
	 */
	private boolean mayVote(String sPlayer) {
		BanVotePlugin.db.i(yes.contains(sPlayer) + " || "
				+ no.contains(sPlayer) + " || " + voter.equals(sPlayer)
				+ " || " + target.equals(sPlayer));
		return !(yes.contains(sPlayer) || no.contains(sPlayer)
				|| voter.equals(sPlayer) || target.equals(sPlayer));
	}

	protected static void set(Map<String, Object> m) {
		stageSeconds = Integer.parseInt(String.valueOf(m.get("StageSeconds")));
		noValue = Float.parseFloat(String.valueOf(m.get("NoValue")));
		yesValue = Float.parseFloat(String.valueOf(m.get("YesValue")));
		afkValue = Float.parseFloat(String.valueOf(m.get("AfkValue")));
		nonValue = Float.parseFloat(String.valueOf(m.get("NonValue")));
		validMin = Float.parseFloat(String.valueOf(m.get("ValidMin")));
		validMax = Float.parseFloat(String.valueOf(m.get("ValidMax")));
		posMinutes = Integer.parseInt(String.valueOf(m.get("PosMinutes")));
		negMinutes = Integer.parseInt(String.valueOf(m.get("NegMinutes")));
		coolMinutes = Integer.parseInt(String.valueOf(m.get("CoolMinutes")));
		calcPublic = Boolean.parseBoolean(String.valueOf(m.get("CalcPublic")));
	}
}
