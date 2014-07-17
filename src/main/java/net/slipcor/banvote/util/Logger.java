package net.slipcor.banvote.util;

import net.slipcor.banvote.BanVotePlugin;

/**
 * ban vote logger class
 *
 * @author slipcor
 * @version v0.0.6
 */

public class Logger {

    /**
     * Check if a given string is empty
     *
     * @param msg a string to check
     * @return true if the string is null or empty, false otherwise
     */
    protected boolean isEmpty(final String msg) {
        return ((msg == null) || (msg.equals("")));
    }

    /**
     * Log a string as INFO message if not empty
     *
     * @param msg a string to send
     */
    public void info(final String msg) {
        if (isEmpty(msg)) {
            return;
        }
        BanVotePlugin.instance.getLogger().info(msg);
    }

    /**
     * Log a string as WARNING message if not empty
     *
     * @param msg a string to send
     */
    public void warn(final String msg) {
        if (isEmpty(msg)) {
            return;
        }
        BanVotePlugin.instance.getLogger().warning(msg);
    }

    /**
     * Log a string as SEVERE message if not empty
     *
     * @param msg a string to send
     */
    protected void severe(final String msg) {
        if (isEmpty(msg)) {
            return;
        }
        BanVotePlugin.instance.getLogger().severe(msg);
    }
}
