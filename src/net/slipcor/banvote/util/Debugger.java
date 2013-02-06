package net.slipcor.banvote.util;

/**
 * ban vote debugger class
 * 
 * @version v0.0.4
 * 
 * @author slipcor
 * 
 */

public class Debugger extends Logger {
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
	public Debugger(final boolean bActive) {
		super();
		active = bActive;
	}

	/**
	 * Check if logging is inactive or the given string is empty
	 * 
	 * @param msg
	 *            a string to check
	 * @return true logging inactive or if the string is null or empty, false
	 *         otherwise
	 */
	@Override
	protected boolean isEmpty(final String msg) {
		return (!active || super.isEmpty(msg));
	}
}
