package net.slipcor.banvote;

import java.util.logging.Logger;

/**
 * ban vote debugger class
 * 
 * @version v0.0.0
 * 
 * @author slipcor
 * 
 */

public class BanVoteDebugger extends BanVoteLogger {
	private final boolean active;

	/**
	 * Create a new BanVote Debugger instance
	 * 
	 * @param lLogger
	 *            referenced logger instance
	 * @param sPrefix
	 *            prefix for messages
	 * @param bActive
	 *            activity state of the debugger
	 */
	public BanVoteDebugger(Logger lLogger, String sPrefix, boolean bActive) {
		super(lLogger, sPrefix);
		active = bActive;
	}

	/**
	 * Check if logging is inactive or the given string is empty
	 * 
	 * @param s
	 *            a string to check
	 * @return true logging inactive or if the string is null or empty, false
	 *         otherwise
	 */
	@Override
	protected boolean isEmpty(String s) {
		if (!active || (s == null) || (s.equals(""))) {
			return true;
		}
		return false;
	}
}
