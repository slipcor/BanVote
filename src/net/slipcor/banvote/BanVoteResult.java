package net.slipcor.banvote;

import org.bukkit.ChatColor;

/**
 * ban vote result class
 * 
 * -
 * 
 * the actual ban/mute resulting from a vote
 * 
 * @version v0.0.3
 * 
 * @author slipcor
 * 
 */

public class BanVoteResult {
	private final long timestamp;
	private final String voter;
	private final String target;
	private final int interval;
	private final boolean result;
	private final byte type; // 0 = mute ; 1 = kick ; 2 = ban

	/**
	 * construct a ban vote result instance
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
	 * @param bType
	 *            0 = mute ; 1 = kick ; 2 = ban
	 */
	public BanVoteResult(int uid, String sVoter, String sTarget, long lTimestamp,
			int iInterval, boolean bResult, byte bType) {
		voter = sVoter;
		target = sTarget;
		timestamp = lTimestamp;
		interval = iInterval;
		result = bResult;
		type = bType;
		BanVotePlugin.instance.getConfig().set("bans.b" + uid, getContents());
		BanVotePlugin.instance.saveConfig();
	}

	/**
	 * check who was banned/muted
	 * 
	 * @return result true: target, else: voter name
	 */
	protected String getResultPlayerName() {
		return result ? target : voter;
	}

	/**
	 * check if the ban/mute has expired
	 * 
	 * @return true if the ban/mute has expired, false otherwise
	 */
	protected boolean over() {
		BanVotePlugin.db.i(timestamp + (interval * 60) + " < "
				+ System.currentTimeMillis() / 1000);
		return (timestamp + (interval * 60)) < System.currentTimeMillis() / 1000;
	}

	/**
	 * combine the ban/mute information to a string
	 * 
	 * @return a string of all information, joined with ":"
	 */
	private String getContents() {
		return voter + ":" + target + ":" + timestamp + ":" + interval + ":"
				+ result + ":" + type;
	}

	/**
	 * print voter, target and result
	 * 
	 * @return a string containing voter, target, banned, interval
	 */
	public String getInfo() {
		String s = "mute";
		if (type == 1) {
			s = "kick";
		} else if (type == 2) {
			s = "ban";
		}

		return ChatColor.GREEN + voter + ChatColor.GOLD + " => "
				+ ChatColor.RED + target + ChatColor.GOLD + "; " + s + ": "
				+ (result ? target : voter) + " (" + interval + " mins)";
	}

	/**
	 * get the ban type
	 * 
	 * @return 0 (mute), 1 (kick), 2 (ban)
	 */
	public byte getType() {
		return type;
	}
}
