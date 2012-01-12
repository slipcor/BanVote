package net.slipcor.banvote;

import java.util.HashMap;
import java.util.HashSet;

/**
 * ban vote result manager class
 * 
 * @version v0.0.3
 * 
 * @author slipcor
 * 
 */

public class BanVoteResultManager {
	HashMap<Integer, BanVoteResult> results = new HashMap<Integer, BanVoteResult>();

	/**
	 * add a string to the bans/mutes
	 * 
	 * @param s
	 *            ban string
	 */
	protected void add(String s) {
		BanVotePlugin.db.i("banning : " + s);
		String[] args = s.split(":");
		int i = getFreeID();
		results.put(
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
	private int getFreeID() {
		int i = 0;
		while (results.get(++i) != null)
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
	protected boolean checkRemove(int i) {
		if (results.get(i) != null && results.get(i).over()) {
			remove(i);
			return true;
		}
		return false;
	}

	/**
	 * check all bans/mutes if they can be removed and do that if possible
	 */
	protected void checkRemove() {
		HashSet<Integer> uids = new HashSet<Integer>();
		for (int i : results.keySet()) {
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
	protected void remove(int i) {
		results.remove(i);
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
	protected HashMap<Integer, String> getList(boolean b) {
		HashMap<Integer, String> result = new HashMap<Integer, String>();

		for (int i : results.keySet()) {
			if ((results.get(i).getType() == 2 && b)
					|| (results.get(i).getType() == 0 && !b))

				result.put(i, results.get(i).getResultPlayerName());
		}

		return result;
	}

	/**
	 * check if a given player name is banned
	 * 
	 * @param sPlayer
	 *            the player name to check
	 * @return true if a ban is active, false otherwise
	 */
	protected boolean isBanned(String sPlayer) {
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
	protected boolean isMuted(String sPlayer) {
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
}
