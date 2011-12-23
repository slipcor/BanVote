package net.slipcor.banvote;

import java.util.logging.Logger;

/**
 * ban vote logger class
 * 
 * @version v0.0.0
 * 
 * @author slipcor
 * 
 */

public class BanVoteLogger {
	private final Logger logger;
	private final String prefix;

	/**
	 * Create a new BanVote Logger instance
	 * 
	 * @param lLogger
	 *            referenced logger instance
	 * @param sPrefix
	 *            prefix for messages
	 */
	public BanVoteLogger(Logger lLogger, String sPrefix) {
		logger = lLogger;
		prefix = sPrefix;
	}

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
		logger.info(prefix + s);
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
		logger.warning(prefix + s);
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
		logger.severe(prefix + s);
	}
}
