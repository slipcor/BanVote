package net.slipcor.banvote;

/**
 * 
 * ban vote command class
 * 
 * @version 0.0.4
 * 
 * @author slipcor
 *
 */

public class BanVoteCommand {
	
	private boolean ban;
	private boolean kick;
	private String command;
	
	/**
	 * create a ban vote command instance
	 * @param bBan do we want to ban?
	 * @param bKick do we want to kick?
	 * @param sCommand the command string we want to perform
	 */
	public BanVoteCommand(boolean bBan, boolean bKick, String sCommand) {
		ban = bBan;
		kick = bKick;
		command = sCommand;
	}
	
	/**
	 * do we want to ban?
	 * @return true if we want to ban, false otherwise
	 */
	public boolean doesBan() {
		return ban;
	}

	/**
	 * do we want to kick?
	 * @return true if we want to ban, false otherwise
	 */
	public boolean doesKick() {
		return kick;
	}
	
	/**
	 * get the command to commit
	 * @return the command string to commit
	 */
	public String getCommand() {
		return command;
	}
}
