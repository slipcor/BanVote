package net.slipcor.banvote;


import java.util.HashMap;
import java.util.HashSet;

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
	 * add a string to the bans/mutes
	 * 
	 * @param s
	 *            ban string
	 */
	protected static void add(String s) {
		BanVotePlugin.db.i("banning : " + s);
		String[] args = s.split(":");
		int i = getFreeID();
		BanVotePlugin.results.put(
				i,
				new BanVoteResult(i, args[0], args[1], Long.parseLong(args[2]),
						Integer.parseInt(args[3]), args[4]
								.equalsIgnoreCase("true"), Byte
								.parseByte(args[5])));
	}

	/**
	 * get the first free id
	 * 
	 * @return an unused index
	 */
	private static int getFreeID() {
		int i = 0;
		while (BanVotePlugin.results.get(++i) != null)
			;
		return i;
	}

	/**
	 * check if a given UID can be removed and do it
	 * 
	 * @param i
	 *            the ban/mute UID
	 * @return true if the ban/mute was removed, false otherwise
	 */
	protected static boolean checkRemove(int i) {
		if (BanVotePlugin.results.get(i) != null && BanVotePlugin.results.get(i).over()) {
			remove(i);
			return true;
		}
		return false;
	}

	/**
	 * check all bans/mutes if they can be removed and do that if possible
	 */
	protected static void checkRemove() {
		HashSet<Integer> uids = new HashSet<Integer>();
		for (int i : BanVotePlugin.results.keySet()) {
			uids.add(i);
		}
		for (int i : uids) {
			checkRemove(i);
		}
	}

	/**
	 * remove a given UID from the bans/mutes
	 * 
	 * @param i
	 *            the ban UID to remove
	 */
	protected static void remove(int i) {
		BanVotePlugin.results.remove(i);
		BanVotePlugin.instance.getConfig().set("bans.b" + i, null);
		BanVotePlugin.instance.saveConfig();
	}

	/**
	 * read a map of UID => banned/muted player name
	 * 
	 * @param b
	 *            banned = true; muted = false
	 * @return a map of all band UIDs mapped to the banned player name
	 */
	protected static HashMap<Integer, String> getList(boolean b) {
		HashMap<Integer, String> result = new HashMap<Integer, String>();

		for (int i : BanVotePlugin.results.keySet()) {
			if ((BanVotePlugin.results.get(i).getType() == 2 && b)
					|| (BanVotePlugin.results.get(i).getType() == 0 && !b)
					|| customBan(BanVotePlugin.results.get(i).getType()))

				result.put(i, BanVotePlugin.results.get(i).getResultPlayerName());
		}

		return result;
	}
	
	/**
	 * is a custom result banning the player?
	 * @param type the command id to check
	 * @return true if the player is to be banned, false otherwise
	 */
	private static boolean customBan(byte type) {
		int i = -3+type;
		
		if (i < 0) {
			return false;
		}
		
		for (BanVoteCommand bc : BanVotePlugin.commands.values()) {
			if (i-- < 1) {
				return bc.doesBan();
			}
		}
		
		return false;
	}

	/**
	 * check if a given player name is banned
	 * 
	 * @param sPlayer
	 *            the player name to check
	 * @return true if a ban is active, false otherwise
	 */
	protected static boolean isBanned(String sPlayer) {
		HashMap<Integer, String> map = getList(true);

		for (int i : map.keySet()) {
			if (map.get(i).equals(sPlayer)) {
				if (!checkRemove(i)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * check if a given player name is muted
	 * 
	 * @param sPlayer
	 *            the player name to check
	 * @return true if a mute is active, false otherwise
	 */
	protected static boolean isMuted(String sPlayer) {
		HashMap<Integer, String> map = getList(false);

		for (int i : map.keySet()) {
			if (map.get(i).equals(sPlayer)) {
				if (!checkRemove(i)) {
					return true;
				}
			}
		}
		return false;
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
