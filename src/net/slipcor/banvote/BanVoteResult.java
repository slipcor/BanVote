package net.slipcor.banvote;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.slipcor.banvote.api.BanVoteCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * ban vote result class
 * 
 * -
 * 
 * the actual ban/mute resulting from a vote
 * 
 * @version v0.0.4
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
	public BanVoteResult(final int uid, final String sVoter, final String sTarget, final long lTimestamp,
			final int iInterval, final boolean bResult, final byte bType) {
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
	 * @param strBan
	 *            ban string
	 */
	public static void add(final String strBan) {
		BanVotePlugin.debug.info("banning : " + strBan);
		final String[] args = strBan.split(":");
		final int freeID = getFreeID();
		BanVotePlugin.results.put(
				freeID,
				new BanVoteResult(freeID, args[0], args[1], Long.parseLong(args[2]),
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
		int pos = 0;
		while (BanVotePlugin.results.get(++pos) != null);
		return pos;
	}

	/**
	 * check if a given UID can be removed and do it
	 * 
	 * @param pos
	 *            the ban/mute UID
	 * @return true if the ban/mute was removed, false otherwise
	 */
	protected static boolean checkRemove(final int pos) {
		if (BanVotePlugin.results.get(pos) != null && BanVotePlugin.results.get(pos).over()) {
			remove(pos);
			return true;
		}
		return false;
	}

	/**
	 * check all bans/mutes if they can be removed and do that if possible
	 */
	protected static void checkRemove() {
		final Set<Integer> uids = new HashSet<Integer>();
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
	 * @param pos
	 *            the ban UID to remove
	 */
	protected static void remove(final int pos) {
		BanVotePlugin.results.remove(pos);
		BanVotePlugin.instance.getConfig().set("bans.b" + pos, null);
		BanVotePlugin.instance.saveConfig();
	}

	/**
	 * read a map of UID => banned/muted player name
	 * 
	 * @param banned
	 *            banned = true; muted = false
	 * @return a map of all band UIDs mapped to the banned player name
	 */
	protected static Map<Integer, String> getList(final boolean banned) {
		final Map<Integer, String> result = new HashMap<Integer, String>();

		for (int i : BanVotePlugin.results.keySet()) {
			if ((BanVotePlugin.results.get(i).getType() == 2 && banned)
					|| (BanVotePlugin.results.get(i).getType() == 0 && !banned)
					|| customBan(BanVotePlugin.results.get(i).getType())) {

				result.put(i, BanVotePlugin.results.get(i).getResultPlayerName());
			}
		}

		return result;
	}
	
	/**
	 * is a custom result banning the player?
	 * @param type the command id to check
	 * @return true if the player is to be banned, false otherwise
	 */
	private static boolean customBan(final byte type) {
		int pos = -3+type;
		
		if (pos < 0) {
			return false;
		}
		
		BanVoteCommand bc = BanVotePlugin.instance.getBVCommand(type);
		
		if (bc != null) {
			return bc.doesBan();
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
	public static boolean isBanned(final String sPlayer) {
		final Player player = Bukkit.getPlayer(sPlayer);
		if ((player != null) && (player.hasPermission("banvote.admin"))) {
			return false;
		}
		final Map<Integer, String> map = getList(true);

		for (int i : map.keySet()) {
			if ((map.get(i).equals(sPlayer)) && !checkRemove(i)) {
				return true;
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
	public static int getBannedSeconds(final String sPlayer) {
		if (!isBanned(sPlayer)) {
			return 0;
		}
		final Map<Integer, String> map = getList(true);

		for (int i : map.keySet()) {
			if ((map.get(i).equals(sPlayer)) && !checkRemove(i)) {
				BanVoteResult result = BanVotePlugin.results.get(i);
				final int remaining = result.remainingSeconds();
				
				return Math.max(remaining, 0);
			}
		}
		return 0;
	}

	/**
	 * check if a given player name is muted
	 * 
	 * @param sPlayer
	 *            the player name to check
	 * @return true if a mute is active, false otherwise
	 */
	public static boolean isMuted(final String sPlayer) {
		final Player player = Bukkit.getPlayer(sPlayer);
		if ((player != null) && (player.hasPermission("banvote.admin"))) {
			return false;
		}
		final Map<Integer, String> map = getList(false);

		for (int i : map.keySet()) {
			if ((map.get(i).equals(sPlayer)) && (!checkRemove(i))) {
				return true;
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
		BanVotePlugin.debug.info(timestamp + (interval * 60) + " < "
				+ System.currentTimeMillis() / 1000);
		return (timestamp + (interval * 60)) < System.currentTimeMillis() / 1000;
	}
	
	/**
	 * return the remaining seconds for a mute/ban
	 * 
	 * @return the seconds remaining
	 */
	protected int remainingSeconds() {
		BanVotePlugin.debug.info(timestamp + (interval * 60) + " < "
				+ System.currentTimeMillis() / 1000);
		return (int) ((timestamp + (interval * 60)) - ( System.currentTimeMillis() / 1000));
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
		String action = "mute";
		if (type == 1) {
			action = "kick";
		} else if (type == 2) {
			action = "ban";
		}

		return ChatColor.GREEN + voter + ChatColor.GOLD + " => "
				+ ChatColor.RED + target + ChatColor.GOLD + "; " + action + ": "
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
