package net.slipcor.banvote;

import org.bukkit.Bukkit;

/**
 * ban vote logger class
 * 
 * @version v0.0.4
 * 
 * @author slipcor
 * 
 */

public class BVLogger {
	private final String prefix = "[BanVote] ";

	/**
	 * Check if a given string is empty
	 * 
	 * @param s
	 *            a string to check
	 * @return true if the string is null or empty, false otherwise
	 */
	protected boolean isEmpty(String s) {
		if ((s == null) || (s.equals(""))) {
			return true;
		}
		return false;
	}

	/**
	 * Log a string as INFO message if not empty
	 * 
	 * @param s
	 *            a string to send
	 */
	protected void i(String s) {
		if (isEmpty(s)) {
			return;
		}
		Bukkit.getLogger().info(prefix + s);
	}

	/**
	 * Log a string as WARNING message if not empty
	 * 
	 * @param s
	 *            a string to send
	 */
	protected void w(String s) {
		if (isEmpty(s)) {
			return;
		}
		Bukkit.getLogger().warning(prefix + s);
	}

	/**
	 * Log a string as SEVERE message if not empty
	 * 
	 * @param s
	 *            a string to send
	 */
	protected void s(String s) {
		if (isEmpty(s)) {
			return;
		}
		Bukkit.getLogger().severe(prefix + s);
	}
}
