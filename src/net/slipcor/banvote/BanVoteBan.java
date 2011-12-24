package net.slipcor.banvote;

import org.bukkit.ChatColor;

/**
 * ban vote ban class
 * 
 * @version v0.0.1
 * 
 * @author slipcor
 * 
 */

public class BanVoteBan {
	private final long timestamp;
	private final String voter;
	private final String target;
	private final int interval;
	private final boolean result;

	/**
	 * construct a ban vote ban instance
	 * 
	 * @param uid
	 *            the case unique ID
	 * @param sVoter
	 *            the voting player
	 * @param sTarget
	 *            the player that has been voted on
	 * @param lTimestamp
	 *            the ban timestamp (in seconds)
	 * @param iInterval
	 *            ban length (in minutes)
	 * @param bResult
	 *            true: target banned, false:
	 */
	public BanVoteBan(int uid, String sVoter, String sTarget, long lTimestamp,
			int iInterval, boolean bResult) {
		voter = sVoter;
		target = sTarget;
		timestamp = lTimestamp;
		interval = iInterval;
		result = bResult;
		BanVotePlugin.instance.getConfig().set("bans.b" + uid, getContents());
		BanVotePlugin.instance.saveConfig();
	}

	/**
	 * check who was banned
	 * 
	 * @return result true: target, else: voter name
	 */
	protected String getBanned() {
		return result ? target : voter;
	}

	/**
	 * check if the ban has expired
	 * 
	 * @return true if the ban has expired, false otherwise
	 */
	protected boolean over() {
		BanVotePlugin.db.i(timestamp + (interval*60) + " < " + System.currentTimeMillis() / 1000);
		return (timestamp + (interval*60)) < System.currentTimeMillis() / 1000;
	}

	/**
	 * combine the ban information to a string
	 * 
	 * @return a string of all information, joined with ":"
	 */
	private String getContents() {
		return voter + ":" + target + ":" + timestamp + ":" + interval + ":"
				+ result;
	}

	public String getInfo() {
		return ChatColor.GREEN + voter + ChatColor.GOLD + " => "
				+ ChatColor.RED + target + ChatColor.GOLD + "; banned: "
				+ (result ? target : voter) + " (" + interval + " mins)";
	}
}
