package net.slipcor.banvote;

import java.util.HashMap;
import java.util.HashSet;

/**
 * ban vote manager class
 * 
 * @version v0.0.1
 * 
 * @author slipcor
 * 
 */

public class BanVoteBanManager {
	HashMap<Integer, BanVoteBan> bans = new HashMap<Integer, BanVoteBan>();
	
	/**
	 * add a string to the bans
	 * @param s ban string
	 */
	protected void add(String s) {
		BanVotePlugin.db.i("banning : "+s);
		String[] args = s.split(":");
		int i = getFreeID();
		bans.put(i, new BanVoteBan(i, args[0],args[1],Long.parseLong(args[2]),Integer.parseInt(args[3]),args[4].equalsIgnoreCase("true")));
	}
	
	/**
	 * get the first free id
	 * @return an unused index
	 */
	private int getFreeID() {
		int i = 0;
		while (bans.get(++i) != null);
		return i;
	}

	/**
	 * check if a given UID can be removed and do it
	 * @param i the ban UID
	 * @return true if the ban was removed, false otherwise
	 */
	protected boolean checkRemove(int i) {
		if (bans.get(i) != null && bans.get(i).over()) {
			remove(i);
			return true;
		}
		return false;
	}
	
	/**
	 * check all bans if they can be removed and do when possible
	 */
	protected void checkRemove() {
		HashSet<Integer> uids = new HashSet<Integer>();
		for (int i : bans.keySet()) {
			uids.add(i);
		}
		for (int i : uids) {
			checkRemove(i);
		}
	}
	
	/**
	 * remove a given UID from the bans
	 * @param i the ban UID to remove
	 */
	protected void remove(int i) {
		bans.remove(i);
		BanVotePlugin.instance.getConfig().set("bans.b"+i, null);
		BanVotePlugin.instance.saveConfig();
	}
	
	/**
	 * read a map of UID => banned player name
	 * @return a map of all band UIDs mapped to the banned player name
	 */
	protected HashMap<Integer, String> getList() {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		
		for (int i : bans.keySet()) {
			result.put(i, bans.get(i).getBanned());
		}
		
		return result;
	}
	
	/**
	 * check if a given player name is banned
	 * @param sPlayer the player name to check
	 * @return true if a ban is active, false otherwise
	 */
	protected boolean isBanned(String sPlayer) {
		HashMap<Integer, String> map = getList();
		
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
